package io.mosip.certify.beniniddataprovider.integration.dto.response;
import io.mosip.certify.beniniddataprovider.integration.dto.response.ConsultationDonneeData;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Root response class for the consultation_donnee API.
 */
@Data
public class ConsultationDonneeApiResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("etat")
    private String etat;

    @JsonProperty("message")
    private String message;

    @JsonProperty("Data")
    private List<ConsultationDonneeData> data;
}
