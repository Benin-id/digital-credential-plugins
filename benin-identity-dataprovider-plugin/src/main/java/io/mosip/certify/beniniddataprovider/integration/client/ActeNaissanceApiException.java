package io.mosip.certify.beniniddataprovider.integration.client;

/** Raised for any transport, auth or parsing failure against the ANIP API. */
public class ActeNaissanceApiException extends Exception {

    public ActeNaissanceApiException(String message) {
        super(message);
    }

    public ActeNaissanceApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
