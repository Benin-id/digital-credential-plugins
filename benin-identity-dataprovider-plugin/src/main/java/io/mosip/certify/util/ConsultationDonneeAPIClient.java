package io.mosip.certify.util;

import io.mosip.certify.beniniddataprovider.integration.dto.response.ConsultationDonneeApiResponse;
import io.mosip.certify.beniniddataprovider.integration.dto.request.ConsultationDonneeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Component
public class ConsultationDonneeAPIClient {
    private static final Logger logger = LoggerFactory.getLogger(ConsultationDonneeAPIClient.class);

    @Value("${anipbj.consultation_donnee.endpoint}")
    private String consultationDonneeEndpoint;

    @Value("${anipbj.otp.login}")
    private String loginHeader;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Call consultation_donnee API with dynamic token.
     */
    public ConsultationDonneeApiResponse consultationDonneeRequest(
            ConsultationDonneeRequest request,
            String tokenConnxtion
    ) throws Exception {
        return executePostRequest(
                consultationDonneeEndpoint,
                request,
                ConsultationDonneeApiResponse.class,
                "ConsultationDonnee",
                tokenConnxtion
        );
    }

    private <T> T executePostRequest(String endpoint, Object request, Class<T> responseType, String actionName, String apiKeyValue) throws Exception {
        long startTime = System.currentTimeMillis();
        HttpURLConnection connection = null;

        try {
            URL url = new URL(endpoint);
            connection = (HttpURLConnection) url.openConnection();

            // Configure request
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Login", loginHeader);
            connection.setRequestProperty("apiKey", apiKeyValue); // 🔥 dynamic value
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);

            // Serialize request
            String jsonInput = objectMapper.writeValueAsString(request);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
            }

            // Handle response
            int responseCode = connection.getResponseCode();
            StringBuilder response = new StringBuilder();

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            responseCode == HttpURLConnection.HTTP_OK
                                    ? connection.getInputStream()
                                    : connection.getErrorStream(),
                            StandardCharsets.UTF_8)
            )) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
            }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return objectMapper.readValue(response.toString(), responseType);
            } else {
                throw new RuntimeException(actionName + " failed. HTTP " + responseCode + " - " + response);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            logger.debug("{} API call completed in {} ms", actionName, System.currentTimeMillis() - startTime);
        }
    }
}
