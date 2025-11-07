package io.mosip.certify.beniniddataprovider.integration.service;
import io.mosip.certify.util.ConsultationDonneeAPIClient;
import io.mosip.certify.api.exception.DataProviderExchangeException;
import io.mosip.certify.api.spi.DataProviderPlugin;
import io.mosip.certify.beniniddataprovider.integration.dto.request.ConsultaArg;
import io.mosip.certify.beniniddataprovider.integration.dto.response.DatosPersona;
import io.mosip.certify.beniniddataprovider.integration.dto.response.ResponseReturn;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import io.mosip.certify.beniniddataprovider.integration.dto.request.ConsultationDonneeRequest;
import io.mosip.certify.beniniddataprovider.integration.dto.response.ConsultationDonneeApiResponse;
import io.mosip.certify.beniniddataprovider.integration.dto.response.ConsultationDonneeData;

import java.util.Map;

@ConditionalOnProperty(value = "mosip.certify.integration.data-provider-plugin", havingValue = "BeninIdentityDataProviderPlugin")
@Component
@Slf4j
public class BeninIdentityDataProviderPlugin implements DataProviderPlugin {
    @Autowired
    private ConsultationDonneeAPIClient  ConsultationDonneeAPIClient;
    @Autowired
    private CacheService cacheService;

    @Value("${mosip.certify.peru-id.data-provider-plugin.nu-dni-usuario}")
    private String nuDniUsuario;

    @Value("${mosip.certify.peru-id.data-provider-plugin.nu-ruc-usuario}")
    private String nuRucUsuario;

    @Value("${mosip.certify.peru-id.data-provider-plugin.password}")
    private String password;
    private static final String ACCESS_TOKEN_HASH = "accessTokenHash";
    @Value("${mosip.certify.peru-id.data-provider-plugin.endpoint-uri}")
    private String endpointUri;
     private  String IndividualId;

    @Override
    public JSONObject fetchData(Map<String, Object> identityDetails) throws DataProviderExchangeException {
        ConsultationDonneeRequest consultationDonneeRequest = new ConsultationDonneeRequest();
        try {
            String individualId = cacheService.getUserInfoTransaction(identityDetails.get("accessTokenHash").toString());

            consultationDonneeRequest.setNPI(individualId);
            String nuConsultaDni ="7844";

            String tokenConnxtion = (String) identityDetails.get("sub");
            ConsultationDonneeApiResponse apiResponse ;
            apiResponse = ConsultationDonneeAPIClient.consultationDonneeRequest(consultationDonneeRequest,tokenConnxtion);

            JSONObject jsonObject = new JSONObject();
            if (apiResponse == null || apiResponse.getData() == null || apiResponse.getData().isEmpty()) {
                throw new Exception("No data found in API response");
            }

            if(apiResponse.getData() != null) {
                ConsultationDonneeData data = apiResponse.getData().get(0);
                jsonObject.put("npi", data.getNpi());
                jsonObject.put("firstName", data.getPrenoms());
                jsonObject.put("lastName", data.getNom());
                jsonObject.put("dateOfBirth", data.getDateDeNaissance());
                jsonObject.put("gender", data.getSexe());
                jsonObject.put("fatherName", data.getNomPere() + " " + data.getPrenomsPere());
                jsonObject.put("motherName", data.getNomMere() + " " + data.getPrenomsMere());
                jsonObject.put("birthPlace", data.getLieuNaissance());
                jsonObject.put("nationality", data.getNationalite());
                jsonObject.put("phoneNumber", data.getBjCountryPhoneCode() + " " + data.getBjMobilePhoneNumber());
                jsonObject.put("email", data.getMphEmail());
                jsonObject.put("profession", data.getProfession());
                jsonObject.put("face", "data:image/jpeg;base64," + data.getPortrait());
                return jsonObject;
            }
        } catch (Exception e) {
            log.error("Failed to fetch response from soap resource.");
            throw new DataProviderExchangeException(e.getMessage());
        }
        throw new DataProviderExchangeException("FAILED_TO_FETCH_DATA");
    }
}
