package io.mosip.certify.beniniddataprovider.integration.dto.request;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlType(propOrder = {"body"})
public class RequestEnvelope {
    @XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private RequestBody body;

    public RequestBody getBody() { return body; }
    public void setBody(RequestBody value) { this.body = value; }
}
