package io.mosip.certify.beniniddataprovider.integration.dto.response;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class ResponseReturn {
    @XmlElement(name = "coResultado")
    private String coResultado;

    @XmlElement(name = "deResultado")
    private String deResultado;

    @XmlElement(name = "datosPersona")
    private DatosPersona datosPersona;

    public String getCoResultado() {
        return coResultado;
    }

    public void setCoResultado(String coResultado) {
        this.coResultado = coResultado;
    }

    public String getDeResultado() {
        return deResultado;
    }

    public void setDeResultado(String deResultado) {
        this.deResultado = deResultado;
    }

    public DatosPersona getDatosPersona() {
        return datosPersona;
    }

    public void setDatosPersona(DatosPersona datosPersona) {
        this.datosPersona = datosPersona;
    }
}