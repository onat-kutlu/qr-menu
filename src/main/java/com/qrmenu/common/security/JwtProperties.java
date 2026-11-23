package com.qrmenu.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT configuration properties loaded from application.yml.
 *
 * Example configuration:
 * ```yaml
 * app:
 *   jwt:
 *     secret: your-secret-key-min-256-bits-long
 *     expiration: 86400000  # 24 hours in milliseconds
 * ```
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /**
     * Secret key for signing JWT tokens.
     * Should be at least 256 bits (32 characters) for HS512 algorithm.
     */
    private String secret = "qrmenu-super-secret-key-for-jwt-tokens-min-256bits-long-key";

    /**
     * Token expiration time in milliseconds.
     * Default: 24 hours (86400000 ms)
     */
    private long expiration = 86400000;

    /**
     * Token refresh threshold in milliseconds.
     * If token is expiring within this time, client should refresh.
     * Default: 5 minutes (300000 ms)
     */
    private long refreshThreshold = 300000;
}
