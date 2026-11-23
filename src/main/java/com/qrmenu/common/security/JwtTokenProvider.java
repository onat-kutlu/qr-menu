package com.qrmenu.common.security;

import com.qrmenu.auth.entity.CompanyUser;
import com.qrmenu.auth.entity.User;
import com.qrmenu.common.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JWT Token Provider for generating and validating JSON Web Tokens.
 *
 * This provider handles:
 * - Token generation for authenticated users
 * - Token validation and verification
 * - Claim extraction from tokens
 * - User role and company information encoding in tokens
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    /**
     * Generate a JWT token for a user.
     * Token includes user ID, email, companies, and roles.
     */
    public String generateToken(User user, List<CompanyUser> companyUsers) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        
        // Include company information
        if (companyUsers != null && !companyUsers.isEmpty()) {
            List<Map<String, Object>> companies = companyUsers.stream()
                    .map(cu -> {
                        Map<String, Object> company = new HashMap<>();
                        company.put("id", cu.getCompany().getId());
                        company.put("name", cu.getCompany().getName());
                        company.put("role", cu.getRole().name());
                        return company;
                    })
                    .collect(Collectors.toList());
            claims.put("companies", companies);
        }
        
        // Include highest role
        UserRole highestRole = user.getHighestRole();
        claims.put("highestRole", highestRole.name());
        claims.put("isAdmin", highestRole.isAdmin());

        return createToken(claims, user.getEmail());
    }

    /**
     * Generate a simple token for authentication testing.
     */
    public String generateToken(String email, UserRole role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("highestRole", role.name());
        claims.put("role", role.name());
        claims.put("isAdmin", role.isAdmin());
        return createToken(claims, email);
    }

    /**
     * Create a token with given claims and subject.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Validate a JWT token.
     *
     * @param token JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extract email (subject) from token.
     */
    public String getEmailFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from token.
     */
    public Long getUserIdFromToken(String token) {
        return getClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extract user role from token.
     */
    public String getRoleFromToken(String token) {
        return getClaim(token, claims -> {
            String highestRole = claims.get("highestRole", String.class);
            return highestRole != null ? highestRole : claims.get("role", String.class);
        });
    }

    /**
     * Extract companies info from token.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getCompaniesFromToken(String token) {
        return getClaim(token, claims -> (List<Map<String, Object>>) claims.get("companies"));
    }

    /**
     * Check if token is expiring soon (within 5 minutes).
     */
    public boolean isTokenExpiringSoon(String token) {
        Date expiration = getExpirationDateFromToken(token);
        Date now = new Date();
        Date soonDate = new Date(now.getTime() + (5 * 60 * 1000)); // 5 minutes
        return expiration.before(soonDate);
    }

    /**
     * Extract expiration date from token.
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    /**
     * Generic method to extract any claim from token.
     */
    private <T> T getClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token.
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get signing key for JWT operations.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }
}
