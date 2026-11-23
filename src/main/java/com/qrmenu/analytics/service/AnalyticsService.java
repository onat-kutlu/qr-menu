package com.qrmenu.analytics.service;

import com.qrmenu.analytics.entity.AnalyticsEvent;
import com.qrmenu.analytics.repository.AnalyticsEventRepository;
import com.qrmenu.auth.entity.MenuVersion;
import com.qrmenu.auth.entity.Restaurant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnalyticsService {

    private final AnalyticsEventRepository analyticsEventRepository;

    public void recordLandingView(Restaurant restaurant, String visitId, String ipAddress, String userAgent) {
        recordEvent("LANDING_VIEW", restaurant, null, visitId, ipAddress, userAgent, null);
    }

    public void recordMenuOpen(Restaurant restaurant, MenuVersion.Language language, String visitId, String ipAddress, String userAgent) {
        recordEvent("MENU_OPEN", restaurant, language, visitId, ipAddress, userAgent, null);
    }

    public void recordEvent(String eventType, Restaurant restaurant, MenuVersion.Language language, String visitId, String ipAddress, String userAgent, String metadata) {
        AnalyticsEvent event = AnalyticsEvent.builder()
                .eventType(eventType)
                .restaurantId(restaurant != null ? restaurant.getId() : null)
                .companyId(restaurant != null && restaurant.getCompany() != null ? restaurant.getCompany().getId() : null)
                .language(language)
                .visitId(visitId)
                .ipHash(hashIp(ipAddress))
                .userAgent(userAgent)
                .metadata(metadata)
                .build();

        analyticsEventRepository.save(event);
        log.info("Analytics event: {} for restaurant {}", eventType, restaurant != null ? restaurant.getId() : null);
    }

    private String hashIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(ipAddress.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
