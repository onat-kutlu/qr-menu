package com.qrmenu.restaurant.service;

import com.qrmenu.analytics.service.AnalyticsService;
import com.qrmenu.auth.entity.MenuVersion;
import com.qrmenu.auth.entity.Restaurant;
import com.qrmenu.auth.entity.SlugHistory;
import com.qrmenu.auth.repository.SlugHistoryRepository;
import com.qrmenu.common.exception.BadRequestException;
import com.qrmenu.menu.repository.MenuVersionRepository;
import com.qrmenu.common.exception.ResourceNotFoundException;
import com.qrmenu.restaurant.dto.PublicMenuOpenResponse;
import com.qrmenu.restaurant.dto.PublicRestaurantResponse;
import com.qrmenu.restaurant.repository.RestaurantRepository;
import com.qrmenu.storage.service.StoragePathValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicRestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuVersionRepository menuVersionRepository;
    private final AnalyticsService analyticsService;
    private final SlugHistoryRepository slugHistoryRepository;
    private final StoragePathValidator storagePathValidator;

    public PublicRestaurantResponse getPublicRestaurantInfo(String slug, String visitId, String ipAddress, String userAgent) {
        SlugResolution resolution = resolveSlug(slug);
        Restaurant restaurant = resolution.restaurant();

        if (restaurant.getStatus() != Restaurant.RestaurantStatus.ACTIVE) {
            throw new ResourceNotFoundException("Restaurant not found");
        }

        List<String> languages = menuVersionRepository.findLiveLanguagesByRestaurantId(restaurant.getId());
        languages = languages == null ? List.of() : languages.stream()
                .map(language -> language.toLowerCase(Locale.ROOT))
                .toList();

        Map<String, String> openUrls = new LinkedHashMap<>();
        for (String language : languages) {
            openUrls.put(language, buildOpenUrl(restaurant.getSlug(), language));
        }

        analyticsService.recordLandingView(restaurant, visitId, ipAddress, userAgent);

        return PublicRestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .slug(restaurant.getSlug())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .logoPath(restaurant.getLogoPath() == null ? null : "/api/public/menus/" + restaurant.getSlug() + "/logo")
                .openUrls(openUrls)
                .openUrl(languages.isEmpty() ? null : openUrls.get(languages.get(0)))
                .redirectUrl(resolution.legacy() ? "/api/public/menus/" + restaurant.getSlug() : null)
                .languages(languages)
                .status(restaurant.getStatus().name())
                .build();
    }

    public PublicMenuOpenResponse openMenu(String slug, String language, String visitId, String ipAddress, String userAgent) {
        Restaurant restaurant = resolveSlug(slug).restaurant();

        if (restaurant.getStatus() != Restaurant.RestaurantStatus.ACTIVE) {
            throw new ResourceNotFoundException("Restaurant not found");
        }

        String lang = language == null || language.isBlank()
            ? "tr"
            : language.trim().toLowerCase(Locale.ROOT);
        MenuVersion.Language requestedLanguage = parseLanguage(lang);
        menuVersionRepository.findLiveByRestaurantAndLanguage(restaurant, requestedLanguage)
            .orElseThrow(() -> new ResourceNotFoundException("No live menu found for the requested language"));

        analyticsService.recordMenuOpen(restaurant, requestedLanguage, visitId, ipAddress, userAgent);

        String pdfUrl = "/api/public/menus/" + restaurant.getSlug() + "/pdf?lang=" + lang;

        return PublicMenuOpenResponse.builder()
                .restaurantSlug(restaurant.getSlug())
                .language(lang)
                .pdfUrl(pdfUrl)
                .redirectUrl(pdfUrl)
                .build();
    }

    public Path getLivePdfPath(String slug, String language, String visitId, String ipAddress, String userAgent) {
        Restaurant restaurant = resolveSlug(slug).restaurant();
        if (restaurant.getStatus() != Restaurant.RestaurantStatus.ACTIVE) {
            throw new ResourceNotFoundException("Restaurant not found");
        }

        String lang = language == null || language.isBlank()
                ? "tr"
                : language.trim().toLowerCase(Locale.ROOT);
        MenuVersion.Language requestedLanguage = parseLanguage(lang);
        MenuVersion liveMenu = menuVersionRepository.findLiveByRestaurantAndLanguage(restaurant, requestedLanguage)
                .orElseThrow(() -> new ResourceNotFoundException("No live menu found for the requested language"));
        Path pdfPath = storagePathValidator.resolve(liveMenu.getFilePath());
        if (!java.nio.file.Files.isRegularFile(pdfPath)) {
            throw new ResourceNotFoundException("Live menu file not found");
        }
        analyticsService.recordMenuOpen(restaurant, requestedLanguage, visitId, ipAddress, userAgent);
        return pdfPath;
    }

    public Path getLogoPath(String slug) {
        Restaurant restaurant = resolveSlug(slug).restaurant();
        if (restaurant.getStatus() != Restaurant.RestaurantStatus.ACTIVE || restaurant.getLogoPath() == null) {
            throw new ResourceNotFoundException("Restaurant logo not found");
        }
        Path logoPath = storagePathValidator.resolve(restaurant.getLogoPath());
        if (!java.nio.file.Files.isRegularFile(logoPath)) {
            throw new ResourceNotFoundException("Restaurant logo not found");
        }
        return logoPath;
    }

    private String buildOpenUrl(String slug, String language) {
        return "/api/public/menus/" + slug + "/pdf?lang=" + language;
    }

    private SlugResolution resolveSlug(String slug) {
        return restaurantRepository.findBySlugIgnoreCase(slug)
                .map(restaurant -> new SlugResolution(restaurant, false))
                .orElseGet(() -> slugHistoryRepository.findFirstByOldSlugIgnoreCaseOrderByCreatedAtDesc(slug)
                        .map(SlugHistory::getRestaurant)
                        .map(restaurant -> new SlugResolution(restaurant, true))
                        .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found")));
    }

    private record SlugResolution(Restaurant restaurant, boolean legacy) {
    }

    private MenuVersion.Language parseLanguage(String language) {
        try {
            return MenuVersion.Language.valueOf(language.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unsupported menu language: " + language);
        }
    }

    public PublicRestaurantResponse getPublicRestaurantInfo(String slug) {
        return getPublicRestaurantInfo(slug, null, null, null);
    }

    public PublicMenuOpenResponse openMenu(String slug, String language) {
        return openMenu(slug, language, null, null, null);
    }
}
