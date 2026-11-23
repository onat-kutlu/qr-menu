package com.qrmenu.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the client address exposed by the reverse proxy, falling back to
 * the servlet remote address for direct connections.
 */
public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest request) {
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",", 2)[0].trim();
        }

        return request.getRemoteAddr();
    }
}
