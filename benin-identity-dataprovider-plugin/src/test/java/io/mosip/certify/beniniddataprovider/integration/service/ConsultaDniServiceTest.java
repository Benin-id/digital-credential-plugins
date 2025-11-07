//package io.mosip.certify.beniniddataprovider.integration.service;
//
//import io.mosip.certify.beniniddataprovider.integration.dto.request.ConsultaArg;
//import io.mosip.certify.beniniddataprovider.integration.dto.response.ConsultarResponse;
//import io.mosip.certify.beniniddataprovider.integration.dto.response.ResponseBody;
//import io.mosip.certify.beniniddataprovider.integration.dto.response.ResponseEnvelope;
//import io.mosip.certify.beniniddataprovider.integration.dto.response.ResponseReturn;
//import io.mosip.certify.util.SoapUtil;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.*;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import static org.junit.Assert.*;
//import static org.mockito.ArgumentMatchers.*;
//
//@RunWith(MockitoJUnitRunner.class)
//public class ConsultaDniServiceTest {
//
//    private static final String SAMPLE_SOAP_REQUEST =
//            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:end=\"http://endpoint.wsconsultadni.reniec.gob.pe/\">\n" +
//                    "   <soapenv:Header/>\n" +
//                    "   <soapenv:Body>\n" +
//                    "      <end:consultar>\n" +
//                    "         <arg0>\n" +
//                    "            <nuDniConsulta>12345678</nuDniConsulta>\n" +
//                    "            <nuDniUsuario>12345678</nuDniUsuario>\n" +
//                    "            <nuRucUsuario>12345678</nuRucUsuario>\n" +
//                    "            <password>12345678</password>\n" +
//                    "         </arg0>\n" +
//                    "      </end:consultar>\n" +
//                    "   </soapenv:Body>\n" +
//                    "</soapenv:Envelope>";
//
//    private static final String SAMPLE_SOAP_RESPONSE =
//            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
//                    "   <soap:Body>\n" +
//                    "      <consultarResponse>\n" +
//                    "         <return>\n" +
//                    "            <coResultado>0000</coResultado>\n" +
//                    "            <deResultado>Success</deResultado>\n" +
//                    "         </return>\n" +
//                    "      </consultarResponse>\n" +
//                    "   </soap:Body>\n" +
//                    "</soap:Envelope>";
//
//    private static final String endpointUri = "https://example.com";
//
//    @InjectMocks
//    private ConsultaDniService consultaDniService;
//
//    @Before
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void getConsultarResponse_Success() throws Exception {
//        // Arrange
//        ConsultaArg consultaArg = new ConsultaArg();
//        consultaArg.setNuDniConsulta("12345678");
//        consultaArg.setNuDniUsuario("12345678");
//        consultaArg.setNuRucUsuario("12345678");
//        consultaArg.setPassword("12345678");
//
//        ResponseEnvelope responseEnvelope = new ResponseEnvelope();
//        ResponseBody responseBody = new ResponseBody();
//        ConsultarResponse consultarResponse = new ConsultarResponse();
//
//        ResponseReturn expectedResponse = new ResponseReturn();
//        expectedResponse.setCoResultado("0000");
//        expectedResponse.setDeResultado("Success");
//        consultarResponse.setResponseReturn(expectedResponse);
//        responseBody.setConsultarResponse(consultarResponse);
//        responseEnvelope.setResponseBody(responseBody);
//
//        try (MockedStatic<SoapUtil> soapUtilMock = Mockito.mockStatic(SoapUtil.class)) {
//            // Mock static methods
//            soapUtilMock.when(() -> SoapUtil.buildSoapRequest(any(ConsultaArg.class)))
//                    .thenReturn(SAMPLE_SOAP_REQUEST);
//
//            soapUtilMock.when(() -> SoapUtil.sendSOAPRequest(anyString(), anyString()))
//                    .thenReturn(SAMPLE_SOAP_RESPONSE);
//
//            soapUtilMock.when(() -> SoapUtil.getResponseEnvelope(anyString()))
//                    .thenReturn(responseEnvelope);
//
//            // Act
//            ResponseReturn result = consultaDniService.getConsultarResponse(consultaArg, endpointUri);
//
//            // Assert
//            assertNotNull(result);
//            assertEquals("0000", result.getCoResultado());
//            assertEquals("Success", result.getDeResultado());
//
//            // Verify static method calls
//            soapUtilMock.verify(() -> SoapUtil.buildSoapRequest(consultaArg));
//        }
//    }
//
//    @Test
//    public void getConsultarResponse_DeserializationError() throws Exception {
//        // Arrange
//        ConsultaArg consultaArg = new ConsultaArg();
//
//        try (MockedStatic<SoapUtil> mockedStatic = Mockito.mockStatic(SoapUtil.class)) {
//            mockedStatic.when(() -> SoapUtil.buildSoapRequest(any(ConsultaArg.class)))
//                    .thenReturn(SAMPLE_SOAP_REQUEST);
//
//            // Mock XmlMapper to throw exception
//            mockedStatic.when(() -> SoapUtil.sendSOAPRequest(anyString(), anyString()))
//                    .thenThrow(new Exception("ERROR_DURING_DESERIALIZATION"));
//
//            // Act & Assert
//            Exception exception = assertThrows(Exception.class, () -> {
//                consultaDniService.getConsultarResponse(consultaArg, endpointUri);
//            });
//            assertEquals("FAILED_TO_GET_RESPONSE", exception.getMessage());
//        }
//    }
//
//    @Test
//    public void getConsultarResponse_NullResponse() throws Exception {
//        // Arrange
//        ConsultaArg consultaArg = new ConsultaArg();
//
//        try (MockedStatic<SoapUtil> mockedStatic = Mockito.mockStatic(SoapUtil.class)) {
//            mockedStatic.when(() -> SoapUtil.buildSoapRequest(any(ConsultaArg.class)))
//                    .thenReturn(SAMPLE_SOAP_REQUEST);
//
//            // Mock XmlMapper to return null
//            mockedStatic.when(() -> SoapUtil.sendSOAPRequest(anyString(), anyString()))
//                    .thenReturn(null);
//
//            // Act & Assert
//            Exception exception = assertThrows(Exception.class, () -> {
//                consultaDniService.getConsultarResponse(consultaArg, endpointUri);
//            });
//            assertEquals("FAILED_TO_GET_RESPONSE", exception.getMessage());
//        }
//    }
//
//    @Test
//    public void getConsultarResponse_SoapUtilError() throws Exception {
//        // Arrange
//        ConsultaArg consultaArg = new ConsultaArg();
//
//        try (MockedStatic<SoapUtil> mockedStatic = Mockito.mockStatic(SoapUtil.class)) {
//            mockedStatic.when(() -> SoapUtil.buildSoapRequest(any(ConsultaArg.class)))
//                    .thenThrow(new Exception("SOAP Request Build Error"));
//
//            // Act & Assert
//            Exception exception = assertThrows(Exception.class, () -> {
//                consultaDniService.getConsultarResponse(consultaArg, endpointUri);
//            });
//            assertEquals("FAILED_TO_GET_RESPONSE", exception.getMessage());
//        }
//    }
//}