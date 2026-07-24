package io.mosip.certify.beniniddataprovider.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.certify.api.exception.DataProviderExchangeException;
import io.mosip.certify.api.spi.DataProviderPlugin;
import io.mosip.certify.beniniddataprovider.integration.client.ActeNaissanceApiClient;
import io.mosip.certify.beniniddataprovider.integration.config.BeninFieldMappingConfig;
import io.mosip.certify.beniniddataprovider.integration.dto.response.ActeNaissanceResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Data provider plugin for Benin.
 *
 * Flow:
 *   1. resolve the NPI (individualId) from the eSignet userinfo cache (Redis)
 *      using the accessTokenHash supplied by Certify;
 *   2. call the ANIP acte-de-naissance API (auth + lookup) through
 *      {@link ActeNaissanceApiClient};
 *   3. project the record onto the VC credential subject using the single
 *      externalised mapping in {@link BeninFieldMappingConfig}.
 *
 * Expression grammar per mapping entry (pointers rooted at the "data" object):
 *   /a/b            single value
 *   X || Y || Z     fallback - first non-blank wins
 *   X + Y + Z       join non-blank parts with a single space
 */
@ConditionalOnProperty(value = "mosip.certify.integration.data-provider-plugin",
        havingValue = "BeninIdentityDataProviderPlugin")
@Component
@Slf4j
public class BeninIdentityDataProviderPlugin implements DataProviderPlugin {

    private static final String ACCESS_TOKEN_HASH = "accessTokenHash";
    private static final String ERROR_FAILED_TO_FETCH = "FAILED_TO_FETCH_DATA";
    private static final String ERROR_INDIVIDUAL_ID_NOT_FOUND = "INDIVIDUAL_ID_NOT_FOUND";

    private static final String OP_FALLBACK = "\\|\\|";
    private static final String OP_JOIN = "\\+";
    private static final String JOIN_SEPARATOR = " ";

    private final ActeNaissanceApiClient acteNaissanceApiClient;
    private final CacheService cacheService;
    private final BeninFieldMappingConfig mappingConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public BeninIdentityDataProviderPlugin(ActeNaissanceApiClient acteNaissanceApiClient,
                                           CacheService cacheService,
                                           BeninFieldMappingConfig mappingConfig) {
        this.acteNaissanceApiClient = acteNaissanceApiClient;
        this.cacheService = cacheService;
        this.mappingConfig = mappingConfig;
    }

    @Override
    public JSONObject fetchData(Map<String, Object> identityDetails) throws DataProviderExchangeException {
        String npi = resolveNpi(identityDetails);

        try {
            ActeNaissanceResponse response = acteNaissanceApiClient.getActeNaissance(npi);
// TEMP debug - shows exactly what deserialized
            try {
                log.info("ANIP parsed response = {}",
                        objectMapper.writeValueAsString(response));
            } catch (Exception ex) {
                log.warn("Could not serialize response for logging", ex);
            }
            log.info("success={}, data present={}, enfant present={}",
                    response.getSuccess(),
                    response.getData() != null,
                    response.getData() != null && response.getData().getEnfant() != null);

            return toCredentialSubject(npi, response);
        } catch (Exception e) {
            log.error("Failed to fetch acte de naissance from the ANIP API", e);
            throw new DataProviderExchangeException(ERROR_FAILED_TO_FETCH);
        }
    }

    private String resolveNpi(Map<String, Object> identityDetails) throws DataProviderExchangeException {
        Object accessTokenHash = identityDetails == null ? null : identityDetails.get(ACCESS_TOKEN_HASH);
        if (accessTokenHash == null) {
            log.error("accessTokenHash is missing from identityDetails");
            throw new DataProviderExchangeException(ERROR_INDIVIDUAL_ID_NOT_FOUND);
        }

        String npi = cacheService.getIndividualId(accessTokenHash.toString());
        if (npi == null || npi.isBlank()) {
            log.error("No individualId (NPI) found in cache for the supplied accessTokenHash");
            throw new DataProviderExchangeException(ERROR_INDIVIDUAL_ID_NOT_FOUND);
        }
        if (npi.endsWith("@npi")) {
            npi = npi.substring(0, npi.length() - "@npi".length());
        }

        return npi;
    }

    /**
     * Projects the ANIP record onto the credential subject using the single
     * configured mapping. Blank results are skipped so the template never
     * renders "null".
     */
    private JSONObject toCredentialSubject(String npi, ActeNaissanceResponse response) throws JSONException {
        JsonNode data = objectMapper.valueToTree(response.getData());
        JSONObject json = new JSONObject();

        for (Map.Entry<String, String> entry : mappingConfig.getMapping().entrySet()) {
            String value = evaluate(data, entry.getValue());
            if (value != null && !value.isBlank()) {
                json.put(entry.getKey(), value.trim());
            }
        }

        // NPI is always present: fall back to the cache value if the mapping produced nothing.
        if (!json.has("npi")) {
            json.put("npi", npi);
        }

        return json;
    }

    /** Evaluates one mapping expression against the data tree. */
    private String evaluate(JsonNode data, String expression) {
        if (expression == null || expression.isBlank()) {
            return null;
        }
        String expr = expression.trim();

        if (expr.contains("||")) {
            for (String path : expr.split(OP_FALLBACK)) {
                String v = readPath(data, path);
                if (v != null && !v.isBlank()) {
                    return v;
                }
            }
            return null;
        }

        if (expr.contains("+")) {
            StringBuilder sb = new StringBuilder();
            for (String path : expr.split(OP_JOIN)) {
                String v = readPath(data, path);
                if (v != null && !v.isBlank()) {
                    if (sb.length() > 0) {
                        sb.append(JOIN_SEPARATOR);
                    }
                    sb.append(v.trim());
                }
            }
            return sb.length() == 0 ? null : sb.toString();
        }

        return readPath(data, expr);
    }

    /** Resolves a JSON Pointer (e.g. {@code /enfant/nom}) to a text value, or null. */
    private String readPath(JsonNode root, String pointer) {
        if (pointer == null || pointer.isBlank()) {
            return null;
        }
        JsonNode node = root.at(pointer.trim());
        return (node.isMissingNode() || node.isNull()) ? null : node.asText();
    }
}