package io.mosip.certify.beniniddataprovider.integration.service;

import io.mosip.esignet.core.dto.OIDCTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class CacheService {

    private static final String USERINFO_CACHE = "userinfo";

    private final CacheManager cacheManager;

    @Value("${mosip.certify.cache.store.individual-id}")
    private boolean storeIndividualId;

    @Value("${mosip.certify.cache.secure.individual-id}")
    private boolean secureIndividualId;

    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Fetches individualId directly from cache using accessTokenHash.
     */
    public String getUserInfoTransaction(String accessTokenHash) {
        if (accessTokenHash == null || accessTokenHash.isEmpty()) {
            log.warn("accessTokenHash is null or empty");
            return null;
        }

        OIDCTransaction transaction = getTransactionFromCache(accessTokenHash);
        if (transaction == null) {
            log.warn("No OIDCTransaction found in cache for token: {}", accessTokenHash);
            return null;
        }

        return getIndividualId(transaction);
    }

    /**
     * Returns the individualId from transaction.
     */
    private String getIndividualId(OIDCTransaction transaction) {
        if (storeIndividualId) {
            log.debug("storeIndividualId is disabled. Returning null.");
            return null;
        }

        if (transaction == null) {
            log.warn("OIDCTransaction is null");
            return null;
        }

        String individualId = transaction.getIndividualId();
        if (individualId == null) {
            log.warn("OIDCTransaction has null individualId");
            return null;
        }

        if (secureIndividualId) {
            log.warn("secureIndividualId=true, but encryption/decryption disabled in this build.");
        }

        return individualId;
    }

    /**
     * Fetches OIDCTransaction from cache by accessTokenHash.
     */
    private OIDCTransaction getTransactionFromCache(String accessTokenHash) {
        try {
            return Objects.requireNonNull(cacheManager.getCache(USERINFO_CACHE))
                    .get(accessTokenHash, OIDCTransaction.class);
        } catch (Exception e) {
            log.error("Error retrieving OIDCTransaction from cache for token: {}", accessTokenHash, e);
            return null;
        }
    }
}
