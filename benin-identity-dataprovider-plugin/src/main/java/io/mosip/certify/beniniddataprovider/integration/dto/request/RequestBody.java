package io.mosip.certify.beniniddataprovider.integration.dto.request;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
public class RequestBody {
    @XmlElement(name = "consultar", namespace = "http://endpoint.wsconsultadni.reniec.gob.pe/")
    private Consultar consultar;

    public Consultar getConsultar() { return consultar; }
    public void setConsultar(Consultar value) { this.consultar = value; }
}
