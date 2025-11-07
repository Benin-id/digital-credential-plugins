package io.mosip.certify.beniniddataprovider.integration.dto.response;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "consultar", namespace = "http://endpoint.wsconsultadni.reniec.gob.pe/")
public class ConsultarResponse {
    @XmlElement(name = "return")
    private ResponseReturn responseReturn;

    public ResponseReturn getResponseReturn() {
        return responseReturn;
    }

    public void setResponseReturn(ResponseReturn responseReturn) {
        this.responseReturn = responseReturn;
    }
}