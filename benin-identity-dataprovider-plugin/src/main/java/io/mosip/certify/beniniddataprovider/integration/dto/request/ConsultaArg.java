package io.mosip.certify.beniniddataprovider.integration.dto.request;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class ConsultaArg {
    @XmlElement(name = "nuDniConsulta")
    private String nuDniConsulta;

    @XmlElement(name = "nuDniUsuario")
    private String nuDniUsuario;

    @XmlElement(name = "nuRucUsuario")
    private String nuRucUsuario;

    @XmlElement(name = "password")
    private String password;

    // Getters and Setters
    public String getNuDniConsulta() { return nuDniConsulta; }
    public void setNuDniConsulta(String value) { this.nuDniConsulta = value; }

    public String getNuDniUsuario() { return nuDniUsuario; }
    public void setNuDniUsuario(String value) { this.nuDniUsuario = value; }

    public String getNuRucUsuario() { return nuRucUsuario; }
    public void setNuRucUsuario(String value) { this.nuRucUsuario = value; }

    public String getPassword() { return password; }
    public void setPassword(String value) { this.password = value; }
}
