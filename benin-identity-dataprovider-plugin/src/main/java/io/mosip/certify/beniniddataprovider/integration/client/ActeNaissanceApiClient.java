package io.mosip.certify.beniniddataprovider.integration.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.certify.beniniddataprovider.integration.dto.request.AuthRequest;
import io.mosip.certify.beniniddataprovider.integration.dto.response.ActeNaissanceResponse;
import io.mosip.certify.beniniddataprovider.integration.dto.response.AuthResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thin client over the two ANIP "acte de naissance" endpoints:
 *
 *   POST {base-url}/auth        -> { login, password }            => { access_token, expires_in }
 *   GET  {base-url}/{npi}       -> Authorization: Bearer <JWT>    => birth record
 *
 * The JWT is cached in memory and reused until shortly before its {@code exp}
 * claim. A 401/403 on the data call triggers exactly one forced re-auth and retry.
 */
@Component
@Slf4j
public class ActeNaissanceApiClient {

    private static final String BEARER = "Bearer ";
    /** Renew the token this long before it actually expires. */
    private static final Duration TOKEN_SKEW = Duration.ofSeconds(30);

    @Value("${mosip.certify.benin-id.data-provider-plugin.base-url}")
    private String baseUrl;

    @Value("${mosip.certify.benin-id.data-provider-plugin.login}")
    private String login;

    @Value("${mosip.certify.benin-id.data-provider-plugin.password}")
    private String password;

    @Value("${mosip.certify.benin-id.data-provider-plugin.connect-timeout-ms:10000}")
    private long connectTimeoutMs;

    @Value("${mosip.certify.benin-id.data-provider-plugin.read-timeout-ms:15000}")
    private long readTimeoutMs;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final ReentrantLock tokenLock = new ReentrantLock();

    private HttpClient httpClient;
    private volatile String cachedToken;
    private volatile Instant cachedTokenExpiry = Instant.EPOCH;

    @PostConstruct
    void init() {
        if (baseUrl != null && baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Fetches the birth record for the given NPI, authenticating on demand.
     */
    public ActeNaissanceResponse getActeNaissance(String npi) throws ActeNaissanceApiException {
        if (npi == null || npi.isBlank()) {
            throw new ActeNaissanceApiException("NPI is null or empty");
        }

        HttpResponse<String> response = callActeNaissance(npi, getToken(false));

        if (response.statusCode() == 401 || response.statusCode() == 403) {
            log.info("Acte-naissance call rejected with {}, refreshing token and retrying once", response.statusCode());
            response = callActeNaissance(npi, getToken(true));
        }

        if (response.statusCode() != 200) {
            throw new ActeNaissanceApiException(
                    "Acte-naissance lookup failed. HTTP " + response.statusCode() + " - " + truncate(response.body()));
        }

        return parseActeNaissance(response.body());
    }

    private HttpResponse<String> callActeNaissance(String npi, String token) throws ActeNaissanceApiException {
        URI uri = URI.create(baseUrl + "/" + npi.trim());
        HttpRequest request = HttpRequest.newBuilder(uri)
                .GET()
                .timeout(Duration.ofMillis(readTimeoutMs))
                .header("Accept", "application/json")
                .header("Authorization", BEARER + token)
                .build();
        return send(request, "acte-naissance");
    }

    /**
     * Returns a usable bearer token, re-authenticating when the cached one is
     * missing, expired, or when {@code forceRefresh} is set.
     */
    private String getToken(boolean forceRefresh) throws ActeNaissanceApiException {
        if (!forceRefresh && isTokenUsable()) {
            return cachedToken;
        }
        tokenLock.lock();
        try {
            // Another thread may have refreshed it while we waited.
            if (!forceRefresh && isTokenUsable()) {
                return cachedToken;
            }
            AuthResponse authResponse = authenticate();
            String token = authResponse.getToken();
            if (token == null || token.isBlank()) {
                throw new ActeNaissanceApiException("Auth response did not contain an access_token");
            }
            cachedToken = token;
            cachedTokenExpiry = resolveExpiry(token, authResponse.getExpiresIn());
            log.debug("Obtained new ANIP token, valid until {}", cachedTokenExpiry);
            return cachedToken;
        } finally {
            tokenLock.unlock();
        }
    }

    private boolean isTokenUsable() {
        return cachedToken != null && Instant.now().isBefore(cachedTokenExpiry.minus(TOKEN_SKEW));
    }

    private AuthResponse authenticate() throws ActeNaissanceApiException {
        String body;
        try {
            body = objectMapper.writeValueAsString(new AuthRequest(login, password));
        } catch (IOException e) {
            throw new ActeNaissanceApiException("Unable to serialize auth request", e);
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/auth"))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .timeout(Duration.ofMillis(readTimeoutMs))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = send(request, "auth");
        if (response.statusCode() != 200) {
            throw new ActeNaissanceApiException(
                    "Authentication failed. HTTP " + response.statusCode() + " - " + truncate(response.body()));
        }
        try {
            return objectMapper.readValue(response.body(), AuthResponse.class);
        } catch (IOException e) {
            throw new ActeNaissanceApiException("Unable to parse auth response", e);
        }
    }

    private HttpResponse<String> send(HttpRequest request, String action) throws ActeNaissanceApiException {
        long start = System.currentTimeMillis();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ActeNaissanceApiException(action + " call failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ActeNaissanceApiException(action + " call interrupted", e);
        } finally {
            log.debug("{} call completed in {} ms", action, System.currentTimeMillis() - start);
        }
    }

    /**
     * Deserializes the whole response body and verifies a record was returned.
     * The body is the full envelope: { "success": ..., "data": { ... } }.
     */
    private ActeNaissanceResponse parseActeNaissance(String body) throws ActeNaissanceApiException {
        ActeNaissanceResponse parsed;
        try {
            parsed = objectMapper.readValue(body, ActeNaissanceResponse.class);
        } catch (IOException e) {
            throw new ActeNaissanceApiException("Unable to parse acte-naissance response", e);
        }

        if (parsed == null
                || Boolean.FALSE.equals(parsed.getSuccess())
                || parsed.getData() == null
                || parsed.getData().getEnfant() == null) {
            throw new ActeNaissanceApiException("No acte-naissance record returned for the given NPI");
        }
        return parsed;
    }

    /** Reads the {@code exp} claim from the JWT; falls back to expires_in, then 5 minutes. */
    private Instant resolveExpiry(String jwt, Long expiresInSeconds) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length >= 2) {
                byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
                JsonNode claims = objectMapper.readTree(payload);
                if (claims.hasNonNull("exp")) {
                    return Instant.ofEpochSecond(claims.get("exp").asLong());
                }
            }
        } catch (Exception e) {
            log.debug("Could not read exp claim from token, falling back to expires_in", e);
        }
        if (expiresInSeconds != null && expiresInSeconds > 0) {
            return Instant.now().plusSeconds(expiresInSeconds);
        }
        return Instant.now().plus(Duration.ofMinutes(5));
    }

    private static String truncate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() <= 500 ? body : body.substring(0, 500) + "...";
    }
}