package io.mosip.certify.beniniddataprovider.integration.service;

import io.mosip.esignet.core.dto.OIDCTransaction;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

/**
 * Resolves the individualId / NPI for a given accessTokenHash from the eSignet
 * "userinfo" transaction in Redis.
 *
 * Two read paths are used:
 *   1. Spring's CacheManager (the normal path);
 *   2. a direct Redis read of "userinfo::{hash}" using JDK deserialization,
 *      which is how eSignet writes the value. This is the fallback for when
 *      Certify's own RedisCacheConfiguration uses a different serializer or key
 *      layout than eSignet.
 */
@Service
@Slf4j
public class CacheService {

    private static final String USERINFO_CACHE = "userinfo";
    private static final String KEY_SEPARATOR = "::";

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${mosip.certify.cache.store.individual-id:true}")
    private boolean storeIndividualId;

    @Value("${mosip.certify.cache.secure.individual-id:false}")
    private boolean secureIndividualId;

    public CacheService(CacheManager cacheManager, RedisConnectionFactory redisConnectionFactory) {
        this.cacheManager = cacheManager;

        // Key as plain string, value as JDK-serialized object - matching eSignet.
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer(
                OIDCTransaction.class.getClassLoader()));
        template.afterPropertiesSet();
        this.redisTemplate = template;
    }

    @PostConstruct
    void logSetup() {
        log.info("CacheService ready. CacheManager={}, caches={}",
                cacheManager.getClass().getSimpleName(), cacheManager.getCacheNames());
    }

    /**
     * @return the bare individualId (NPI), or null when it cannot be resolved.
     */
    public String getIndividualId(String accessTokenHash) {
        if (accessTokenHash == null || accessTokenHash.isBlank()) {
            log.warn("accessTokenHash is null or empty");
            return null;
        }

        if (!storeIndividualId) {
            log.warn("mosip.certify.cache.store.individual-id is false; the NPI is not available from cache");
            return null;
        }

        OIDCTransaction transaction = readViaCacheManager(accessTokenHash);
        if (transaction == null) {
            transaction = readDirectFromRedis(accessTokenHash);
        }

        if (transaction == null) {
            log.warn("No OIDCTransaction found for accessTokenHash '{}' via either read path",
                    accessTokenHash);
            return null;
        }

        String individualId = transaction.getIndividualId();
        if (individualId == null || individualId.isBlank()) {
            log.warn("OIDCTransaction found but it contains no individualId");
            return null;
        }

        if (secureIndividualId) {
            log.warn("mosip.certify.cache.secure.individual-id is true but decryption is not implemented here");
        }

        return stripIdTypeSuffix(individualId);
    }

    /** Normal path: Spring's cache abstraction. */
    private OIDCTransaction readViaCacheManager(String accessTokenHash) {
        try {
            Cache cache = cacheManager.getCache(USERINFO_CACHE);
            if (cache == null) {
                log.warn("Cache '{}' is not configured. Available: {}",
                        USERINFO_CACHE, cacheManager.getCacheNames());
                return null;
            }
            OIDCTransaction transaction = cache.get(accessTokenHash, OIDCTransaction.class);
            if (transaction == null) {
                log.debug("CacheManager lookup returned null for '{}', trying direct Redis read",
                        accessTokenHash);
            }
            return transaction;
        } catch (Exception e) {
            log.warn("CacheManager lookup failed for '{}', trying direct Redis read: {}",
                    accessTokenHash, e.getMessage());
            return null;
        }
    }

    /** Fallback path: read "userinfo::{hash}" directly with JDK deserialization. */
    private OIDCTransaction readDirectFromRedis(String accessTokenHash) {
        String key = USERINFO_CACHE + KEY_SEPARATOR + accessTokenHash;
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                log.warn("Direct Redis read found no value at key '{}'", key);
                return null;
            }
            if (!(value instanceof OIDCTransaction)) {
                log.warn("Value at key '{}' is a {}, not an OIDCTransaction",
                        key, value.getClass().getName());
                return null;
            }
            log.info("Resolved OIDCTransaction via direct Redis read at key '{}'", key);
            return (OIDCTransaction) value;
        } catch (Exception e) {
            log.error("Direct Redis read failed for key '" + key + "'", e);
            return null;
        }
    }

    /** eSignet stores e.g. "8820267980@npi"; ANIP expects the bare NPI. */
    private String stripIdTypeSuffix(String individualId) {
        int at = individualId.indexOf('@');
        return ((at > 0) ? individualId.substring(0, at) : individualId).trim();
    }
}