package io.mosip.certify.beniniddataprovider.integration.dto.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Response of GET {base-url}/{npi}.
 *
 * Shape confirmed against the ANIP test endpoint. Every block carries a
 * {@link JsonAnySetter} catch-all ({@code additionalProperties}) so any field
 * ANIP adds later is preserved and reaches the VC template without a code
 * change.
 *
 * <pre>
 * {
 *   "success": true,
 *   "data": {
 *     "npi", "numeroFormulaire", "referenceActe",
 *     "enfant":  { nom, prenoms, sexe, dateNaissance, lieuNaissance,
 *                  nationalite, profession, domicile, npi },
 *     "pere":    { nom, prenoms, dateNaissance, lieuNaissance, profession, domicile, npi },
 *     "mere":    { nom, prenoms, dateNaissance, lieuNaissance, profession, domicile, npi },
 *     "naissance":      { date, lieu, departementCode, communeCode,
 *                         arrondissementCode, villageCode, paysCode },
 *     "enregistrement": { centreDeclarant, departementCode, communeCode,
 *                         arrondissementCode, villageCode },
 *     "sources": [ "..." ]
 *   }
 * }
 * </pre>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActeNaissanceResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("data")
    private Data data;

    /** Common base giving every nested block an extensible catch-all. */
    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public abstract static class Extensible implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

        @JsonAnySetter
        public void set(String key, Object value) {
            additionalProperties.put(key, value);
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return additionalProperties;
        }
    }

    @lombok.Data
    @lombok.EqualsAndHashCode(callSuper = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data extends Extensible {
        private static final long serialVersionUID = 1L;

        @JsonProperty("npi")
        private String npi;

        @JsonProperty("numeroFormulaire")
        private String numeroFormulaire;

        @JsonProperty("referenceActe")
        private String referenceActe;

        @JsonProperty("enfant")
        private Enfant enfant;

        @JsonProperty("pere")
        private Parent pere;

        @JsonProperty("mere")
        private Parent mere;

        @JsonProperty("naissance")
        private Naissance naissance;

        @JsonProperty("enregistrement")
        private Enregistrement enregistrement;

        @JsonProperty("sources")
        private List<String> sources;
    }

    @lombok.Data
    @lombok.EqualsAndHashCode(callSuper = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Enfant extends Extensible {
        private static final long serialVersionUID = 1L;

        @JsonProperty("nom")
        private String nom;

        @JsonProperty("prenoms")
        private String prenoms;

        @JsonProperty("sexe")
        private String sexe;

        @JsonProperty("dateNaissance")
        private String dateNaissance;

        @JsonProperty("lieuNaissance")
        private String lieuNaissance;

        @JsonProperty("nationalite")
        private String nationalite;

        @JsonProperty("profession")
        private String profession;

        @JsonProperty("domicile")
        private String domicile;

        @JsonProperty("npi")
        private String npi;
    }

    @lombok.Data
    @lombok.EqualsAndHashCode(callSuper = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Parent extends Extensible {
        private static final long serialVersionUID = 1L;

        @JsonProperty("nom")
        private String nom;

        @JsonProperty("prenoms")
        private String prenoms;

        @JsonProperty("dateNaissance")
        private String dateNaissance;

        @JsonProperty("lieuNaissance")
        private String lieuNaissance;

        @JsonProperty("profession")
        private String profession;

        @JsonProperty("domicile")
        private String domicile;

        @JsonProperty("npi")
        private String npi;
    }

    @lombok.Data
    @lombok.EqualsAndHashCode(callSuper = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Naissance extends Extensible {
        private static final long serialVersionUID = 1L;

        @JsonProperty("date")
        private String date;

        @JsonProperty("lieu")
        private String lieu;

        @JsonProperty("departementCode")
        private String departementCode;

        @JsonProperty("communeCode")
        private String communeCode;

        @JsonProperty("arrondissementCode")
        private String arrondissementCode;

        @JsonProperty("villageCode")
        private String villageCode;

        @JsonProperty("paysCode")
        private String paysCode;
    }

    @lombok.Data
    @lombok.EqualsAndHashCode(callSuper = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Enregistrement extends Extensible {
        private static final long serialVersionUID = 1L;

        @JsonProperty("centreDeclarant")
        private String centreDeclarant;

        @JsonProperty("departementCode")
        private String departementCode;

        @JsonProperty("communeCode")
        private String communeCode;

        @JsonProperty("arrondissementCode")
        private String arrondissementCode;

        @JsonProperty("villageCode")
        private String villageCode;
    }
}