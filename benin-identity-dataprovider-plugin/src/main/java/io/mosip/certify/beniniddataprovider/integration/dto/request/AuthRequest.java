package io.mosip.certify.beniniddataprovider.integration.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload for POST {base-url}/auth
 * { "login": "...", "password": "..." }
 */
public record AuthRequest(@JsonProperty("login") String login,
                          @JsonProperty("password") String password) {
}
