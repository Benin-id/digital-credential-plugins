package io.mosip.certify.beniniddataprovider.integration.service;

import io.mosip.certify.api.exception.DataProviderExchangeException;
import io.mosip.certify.beniniddataprovider.integration.client.ActeNaissanceApiClient;
import io.mosip.certify.beniniddataprovider.integration.client.ActeNaissanceApiException;
import io.mosip.certify.beniniddataprovider.integration.config.BeninFieldMappingConfig;
import io.mosip.certify.beniniddataprovider.integration.dto.response.ActeNaissanceResponse;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BeninIdentityDataProviderPluginTest {

    private static final String TOKEN_HASH = "hash-123";
    private static final String NPI = "8820267980";

    @Mock
    private ActeNaissanceApiClient acteNaissanceApiClient;

    @Mock
    private CacheService cacheService;

    // Real config bean (not a mock) so the expression evaluator runs for real.
    private final BeninFieldMappingConfig mappingConfig = new BeninFieldMappingConfig();

    private BeninIdentityDataProviderPlugin plugin;

    @Before
    public void setUp() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("npi", "/enfant/npi || /npi");
        mapping.put("lastName", "/enfant/nom");
        mapping.put("firstName", "/enfant/prenoms");
        mapping.put("fullName", "/enfant/prenoms + /enfant/nom");
        mapping.put("gender", "/enfant/sexe");
        mapping.put("dateOfBirth", "/enfant/dateNaissance || /naissance/date");
        mapping.put("fatherName", "/pere/prenoms + /pere/nom");
        mapping.put("motherName", "/mere/prenoms + /mere/nom");
        mappingConfig.setMapping(mapping);

        plugin = new BeninIdentityDataProviderPlugin(acteNaissanceApiClient, cacheService, mappingConfig);
    }

    private ActeNaissanceResponse sampleRecord() {
        ActeNaissanceResponse.Enfant enfant = new ActeNaissanceResponse.Enfant();
        enfant.setNpi(NPI);
        enfant.setNom("DOSSOU");
        enfant.setPrenoms("Marie");
        enfant.setSexe("F");
        enfant.setDateNaissance("1990-01-01");

        ActeNaissanceResponse.Parent pere = new ActeNaissanceResponse.Parent();
        pere.setNom("DOSSOU");
        pere.setPrenoms("Jean");

        ActeNaissanceResponse.Data data = new ActeNaissanceResponse.Data();
        data.setNpi(NPI);
        data.setEnfant(enfant);
        data.setPere(pere);
        // mere deliberately left null

        ActeNaissanceResponse response = new ActeNaissanceResponse();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    @Test
    public void fetchData_success() throws Exception {
        when(cacheService.getIndividualId(TOKEN_HASH)).thenReturn(NPI);
        when(acteNaissanceApiClient.getActeNaissance(NPI)).thenReturn(sampleRecord());

        JSONObject result = plugin.fetchData(Map.of("accessTokenHash", TOKEN_HASH));

        assertEquals(NPI, result.getString("npi"));
        assertEquals("Marie", result.getString("firstName"));
        assertEquals("DOSSOU", result.getString("lastName"));
        assertEquals("Marie DOSSOU", result.getString("fullName"));
        assertEquals("Jean DOSSOU", result.getString("fatherName"));
        // mere was null, so no motherName key is emitted
        assertFalse(result.has("motherName"));
    }

    @Test
    public void fetchData_npiNotInCache() {
        when(cacheService.getIndividualId(TOKEN_HASH)).thenReturn(null);

        DataProviderExchangeException ex = assertThrows(DataProviderExchangeException.class,
                () -> plugin.fetchData(Map.of("accessTokenHash", TOKEN_HASH)));
        assertEquals("INDIVIDUAL_ID_NOT_FOUND", ex.getMessage());
    }

    @Test
    public void fetchData_missingAccessTokenHash() {
        assertThrows(DataProviderExchangeException.class, () -> plugin.fetchData(Map.of()));
    }

    @Test
    public void fetchData_apiFailure() throws Exception {
        when(cacheService.getIndividualId(TOKEN_HASH)).thenReturn(NPI);
        when(acteNaissanceApiClient.getActeNaissance(anyString()))
                .thenThrow(new ActeNaissanceApiException("HTTP 500"));

        DataProviderExchangeException ex = assertThrows(DataProviderExchangeException.class,
                () -> plugin.fetchData(Map.of("accessTokenHash", TOKEN_HASH)));
        assertEquals("FAILED_TO_FETCH_DATA", ex.getMessage());
    }
}