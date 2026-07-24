package io.mosip.certify.beniniddataprovider.integration.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Response of POST {base-url}/auth.
 * The token key is accepted under several common spellings so the plugin keeps
 * working if ANIP renames it.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("token")
    @JsonAlias({"access_token", "accessToken", "jwt", "id_token"})
    private String token;

    @JsonProperty("expires_in")
    @JsonAlias({"expiresIn", "exp"})
    private Long expiresIn;
}
