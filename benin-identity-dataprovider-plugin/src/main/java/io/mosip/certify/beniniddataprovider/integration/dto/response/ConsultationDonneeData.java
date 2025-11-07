package io.mosip.certify.beniniddataprovider.integration.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Represents an individual's detailed information returned in the "Data" array.
 */
@Data
public class ConsultationDonneeData implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("NPI")
    private String npi;

    @JsonProperty("NOM")
    private String nom;

    @JsonProperty("PRENOMS")
    private String prenoms;

    @JsonProperty("DATE_DE_NAISSANCE")
    private String dateDeNaissance;

    @JsonProperty("NOM_PERE")
    private String nomPere;

    @JsonProperty("PRENOMS_PERE")
    private String prenomsPere;

    @JsonProperty("NOM_MERE")
    private String nomMere;

    @JsonProperty("PRENOMS_MERE")
    private String prenomsMere;

    @JsonProperty("SEXE")
    private String sexe;

    @JsonProperty("PAYS_NAISSANCE")
    private String paysNaissance;

    @JsonProperty("DEPARTEMENT_NAISSANCE")
    private String departementNaissance;

    @JsonProperty("COMMUNE_NAISSANCE")
    private String communeNaissance;

    @JsonProperty("ARRONDISSEMENT_NAISSANCE")
    private String arrondissementNaissance;

    @JsonProperty("VILLAGE_QUARTIER_NAISSANCE")
    private String villageQuartierNaissance;

    @JsonProperty("LIEU_NAISSANCE")
    private String lieuNaissance;

    @JsonProperty("PAYS_NAISSANCE_CODE")
    private String paysNaissanceCode;

    @JsonProperty("DEPARTEMENT_NAISSANCE_CODE")
    private String departementNaissanceCode;

    @JsonProperty("COMMUNE_NAISSANCE_CODE")
    private String communeNaissanceCode;

    @JsonProperty("ARRONDISSEMENT_NAISSANCE_CODE")
    private String arrondissementNaissanceCode;

    @JsonProperty("VILLAGE_QUARTIER_NAISSANCE_CODE")
    private String villageQuartierNaissanceCode;

    @JsonProperty("PAYS_RESIDENCE")
    private String paysResidence;

    @JsonProperty("DEPARTEMENT_RESIDENCE")
    private String departementResidence;

    @JsonProperty("COMMUNE_RESIDENCE")
    private String communeResidence;

    @JsonProperty("ARRONDISSEMENT_RESIDENCE")
    private String arrondissementResidence;

    @JsonProperty("VILLAGE_QUARTIER_RESIDENCE")
    private String villageQuartierResidence;

    @JsonProperty("LIEU_RESIDENCE")
    private String lieuResidence;

    @JsonProperty("PROFESSION")
    private String profession;

    @JsonProperty("CODE_NATIONALITE")
    private String codeNationalite;

    @JsonProperty("NATIONALITE")
    private String nationalite;

    @JsonProperty("BJ_COUNTRY_PHONE_CODE")
    private String bjCountryPhoneCode;

    @JsonProperty("BJ_MOBILE_PHONE_NUMBER")
    private String bjMobilePhoneNumber;

    @JsonProperty("MPH_EMAIL")
    private String mphEmail;

    @JsonProperty("PORTRAIT")
    private String portrait;

    @JsonProperty("SIGNATURE")
    private String signature;

    @JsonProperty("TOKEN_CONNEXION")
    private String tokenConnexion;

    @JsonProperty("LISTE_RESIDENCE")
    private List<ResidenceInfo> listeResidence;

    @JsonProperty("PIECES_JOINTES")
    private List<AttachmentInfo> piecesJointes;

    /** Represents one entry from "LISTE_RESIDENCE". */
    @Data
    public static class ResidenceInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @JsonProperty("TYPE_RESIDENCE")
        private String typeResidence;

        @JsonProperty("NUM_TYPE_RESIDENCE")
        private Integer numTypeResidence;

        @JsonProperty("PAYS_RESIDENCE")
        private String paysResidence;

        @JsonProperty("DEPARTEMENT_RESIDENCE")
        private String departementResidence;

        @JsonProperty("COMMUNE_RESIDENCE")
        private String communeResidence;

        @JsonProperty("ARRONDISSEMENT_RESIDENCE")
        private String arrondissementResidence;

        @JsonProperty("VILLAGE_QUARTIER_RESIDENCE")
        private String villageQuartierResidence;

        @JsonProperty("LIEU_RESIDENCE")
        private String lieuResidence;
    }

    /** Represents one entry from "PIECES_JOINTES". */
    @Data
    public static class AttachmentInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @JsonProperty("TYPE_PIECE")
        private String typePiece;

        @JsonProperty("VALEUR")
        private String valeur;
    }
}
