package io.mosip.certify.beniniddataprovider.integration.dto.request;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "consultar", namespace = "http://endpoint.wsconsultadni.reniec.gob.pe/")
public class Consultar {
    @XmlElement(name = "arg0")
    private ConsultaArg arg0;

    public ConsultaArg getArg0() { return arg0; }
    public void setArg0(ConsultaArg value) { this.arg0 = value; }
}
