package com.qrmenu.common.config;

import com.qrmenu.common.util.ClientIpResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int PUBLIC_REQUESTS_PER_MINUTE = 60;
    private static final int PDF_REQUESTS_PER_MINUTE = 10;
    private static final int LOGIN_REQUESTS_PER_MINUTE = 5;
    private static final int WINDOW_SECONDS = 60;

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String clientKey = ClientIpResolver.resolve(request);

        if (clientKey == null || clientKey.isBlank() || path == null || path.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        int limit = resolveLimit(path);
        if (limit == 0) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestKey = buildRequestKey(request, path);
        String redisKey = "rate-limit:" + clientKey + ":" + requestKey;

        Long currentCount = redisTemplate.opsForValue().increment(redisKey);
        if (currentCount != null && currentCount == 1L) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(WINDOW_SECONDS));
        }

        if (currentCount != null && currentCount > limit) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"Too many requests\",\"code\":\"RATE_LIMITED\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private int resolveLimit(String path) {
        if (path.startsWith("/api/auth/")) {
            return LOGIN_REQUESTS_PER_MINUTE;
        }

        if (path.matches("/api/public/menus/[^/]+/pdf")) {
            return PDF_REQUESTS_PER_MINUTE;
        }

        if (path.contains("/api/public/") || path.contains("/qr/")) {
            return PUBLIC_REQUESTS_PER_MINUTE;
        }

        return 0;
    }

    private String buildRequestKey(HttpServletRequest request, String path) {
        if (path.matches("/api/public/menus/[^/]+/(open|pdf)")) {
            String language = request.getParameter("lang");
            return path + ":lang=" + (language == null || language.isBlank() ? "default" : language.toLowerCase());
        }
        return path;
    }
}
