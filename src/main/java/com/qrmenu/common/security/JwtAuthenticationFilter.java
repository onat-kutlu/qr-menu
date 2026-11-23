package com.qrmenu.common.security;

import com.qrmenu.auth.entity.CompanyUser;
import com.qrmenu.auth.entity.User;
import com.qrmenu.auth.repository.CompanyUserRepository;
import com.qrmenu.auth.repository.UserRepository;
import com.qrmenu.common.enums.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT Authentication Filter.
 *
 * This filter:
 * - Extracts JWT token from Authorization header
 * - Validates token signature and expiration
 * - Checks token validity in Redis (blacklist check)
 * - Sets up Spring Security context if token is valid
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final CompanyUserRepository companyUserRepository;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (jwt != null && jwtTokenProvider.validateToken(jwt) && isTokenNotBlacklisted(jwt)) {
                String email = jwtTokenProvider.getEmailFromToken(jwt);
                Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
                String role = resolveCurrentRole(userId);

                if (userId == null || role == null || role.isBlank() || !isTokenActive(jwt)) {
                    log.warn("JWT is not active or has no current role for user: {}", email);
                    filterChain.doFilter(request, response);
                    return;
                }

                // Create authentication token
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userId != null ? userId.toString() : email,
                        null,
                        authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Set Spring Security Authenticated User: {}", email);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header.
     *
     * @param request HTTP request
     * @return JWT token or null if not found/invalid
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * Check if token is not blacklisted.
     *
     * @param token JWT token
     * @return true if token is NOT blacklisted
     */
    private boolean isTokenNotBlacklisted(String token) {
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
        return !Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
    }

    private boolean isTokenActive(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("token:" + token));
    }

    private String resolveCurrentRole(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userRepository.findActiveById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        if (user.getGlobalRole() != null && user.getGlobalRole().isAdmin()) {
            return user.getGlobalRole().name();
        }
        return companyUserRepository.findActiveByUserId(userId).stream()
                .map(CompanyUser::getRole)
                .filter(java.util.Objects::nonNull)
                .min(java.util.Comparator.comparingInt(this::roleRank))
                .map(Enum::name)
                .orElse(null);
    }

    private int roleRank(UserRole role) {
        return switch (role) {
            case SUPER_ADMIN -> 0;
            case ADMIN -> 1;
            case COMPANY_ADMIN -> 2;
            case COMPANY_USER -> 3;
        };
    }
}
