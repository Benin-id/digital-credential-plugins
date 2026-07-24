package io.mosip.certify.beniniddataprovider.integration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single externalised map from the ANIP acte-naissance payload to the VC
 * credential subject. One entry per output field.
 *
 * <pre>
 * mosip.certify.benin-id.data-provider-plugin.mapping.lastName=/enfant/nom
 * mosip.certify.benin-id.data-provider-plugin.mapping.fullName=/enfant/prenoms + /enfant/nom
 * mosip.certify.benin-id.data-provider-plugin.mapping.dateOfBirth=/enfant/dateNaissance || /naissance/date
 * </pre>
 *
 * Expression grammar (all pointers are RFC 6901, rooted at the "data" object):
 *   /a/b            resolve a single value
 *   X || Y || Z     fallback - first non-blank wins
 *   X + Y + Z       join non-blank parts with a single space
 */
@Configuration
@ConfigurationProperties(prefix = "mosip.certify.benin-id.data-provider-plugin")
@Data
public class BeninFieldMappingConfig {

    /** outputKey -> expression. */
    private Map<String, String> mapping = new LinkedHashMap<>();
}
