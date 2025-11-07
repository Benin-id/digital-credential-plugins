//package io.mosip.certify.beniniddataprovider.integration.service;
//
//import io.mosip.certify.api.exception.DataProviderExchangeException;
//import io.mosip.certify.beniniddataprovider.integration.dto.request.ConsultaArg;
//import io.mosip.certify.beniniddataprovider.integration.dto.response.DatosPersona;
//import io.mosip.certify.beniniddataprovider.integration.dto.response.ResponseReturn;
//import org.json.JSONObject;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.MockitoJUnitRunner;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.junit.Assert.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@RunWith(MockitoJUnitRunner.class)
//public class PeruIdentityDataProviderPluginTest {
//
//    @Mock
//    private ConsultaDniService consultaDniService;
//
//    @InjectMocks
//    private PeruIdentityDataProviderPlugin plugin;
//
//    private static final String NU_DNI_USUARIO = "testDniUsuario";
//    private static final String NU_RUC_USUARIO = "testRucUsuario";
//    private static final String PASSWORD = "testPassword";
//    private static final String endpointUri = "https://example.com";
//
//    @Before
//    public void setUp() {
//        ReflectionTestUtils.setField(plugin, "nuDniUsuario", NU_DNI_USUARIO);
//        ReflectionTestUtils.setField(plugin, "nuRucUsuario", NU_RUC_USUARIO);
//        ReflectionTestUtils.setField(plugin, "password", PASSWORD);
//        ReflectionTestUtils.setField(plugin, "endpointUri", endpointUri);
//    }
//
//    @Test
//    public void fetchData_Success() throws Exception {
//        // Arrange
//        Map<String, Object> identityDetails = new HashMap<>();
//        String testDni = "12345678";
//        identityDetails.put("sub", testDni);
//
//        ResponseReturn responseReturn = new ResponseReturn();
//        responseReturn.setCoResultado("0000");
//        responseReturn.setDeResultado("Success");
//
//        DatosPersona datosPersona = new DatosPersona();
//        datosPersona.setDni(testDni);
//        datosPersona.setPrenombres("John");
//        datosPersona.setPrimerApellido("Doe");
//        datosPersona.setSegundoApellido("Smith");
//        datosPersona.setFechaNacimiento("1990-01-01");
//        datosPersona.setGenero("M");
//        datosPersona.setEstadoCivil("Single");
//        datosPersona.setRestriccion("None");
//        datosPersona.setFoto("base64Photo");
//
//        responseReturn.setDatosPersona(datosPersona);
//
//        when(consultaDniService.getConsultarResponse(any(ConsultaArg.class), anyString()))
//                .thenReturn(responseReturn);
//
//        // Act
//        JSONObject result = plugin.fetchData(identityDetails);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(testDni, result.getString("dni"));
//        assertEquals("John", result.getString("firstName"));
//        assertEquals("Doe", result.getString("firstLastName"));
//        assertEquals("Smith", result.getString("secondLastName"));
//        assertEquals("1990-01-01", result.getString("dateOfBirth"));
//        assertEquals("M", result.getString("gender"));
//        assertEquals("Single", result.getString("maritalStatus"));
//        assertEquals("None", result.getString("restriction"));
//        assertEquals("base64Photo", result.getString("face"));
//    }
//
//    @Test
//    public void fetchData_ErrorResponse() throws Exception {
//        // Arrange
//        Map<String, Object> identityDetails = new HashMap<>();
//        identityDetails.put("sub", "12345678");
//
//        ResponseReturn responseReturn = new ResponseReturn();
//        responseReturn.setCoResultado("9999");
//        responseReturn.setDeResultado("Error occurred");
//
//        when(consultaDniService.getConsultarResponse(any(ConsultaArg.class), anyString()))
//                .thenReturn(responseReturn);
//
//        // Act & Assert
//        DataProviderExchangeException exception = assertThrows(
//                DataProviderExchangeException.class,
//                () -> plugin.fetchData(identityDetails)
//        );
//        assertEquals("Error occurred", exception.getMessage());
//    }
//
//    @Test
//    public void fetchData_NullDatosPersona() throws Exception {
//        // Arrange
//        Map<String, Object> identityDetails = new HashMap<>();
//        identityDetails.put("sub", "12345678");
//
//        ResponseReturn responseReturn = new ResponseReturn();
//        responseReturn.setCoResultado("0000");
//        responseReturn.setDeResultado("Success");
//        responseReturn.setDatosPersona(null);
//
//        when(consultaDniService.getConsultarResponse(any(ConsultaArg.class), anyString()))
//                .thenReturn(responseReturn);
//
//        // Act & Assert
//        DataProviderExchangeException exception = assertThrows(
//                DataProviderExchangeException.class,
//                () -> plugin.fetchData(identityDetails)
//        );
//        assertEquals("FAILED_TO_FETCH_DATA", exception.getMessage());
//    }
//}