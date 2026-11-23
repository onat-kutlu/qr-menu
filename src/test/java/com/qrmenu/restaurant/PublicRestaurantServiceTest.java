package com.qrmenu.restaurant;

import com.qrmenu.analytics.service.AnalyticsService;
import com.qrmenu.auth.entity.Company;
import com.qrmenu.auth.entity.MenuVersion;
import com.qrmenu.auth.entity.Restaurant;
import com.qrmenu.auth.entity.User;
import com.qrmenu.common.exception.ResourceNotFoundException;
import com.qrmenu.menu.repository.MenuVersionRepository;
import com.qrmenu.auth.repository.SlugHistoryRepository;
import com.qrmenu.restaurant.dto.PublicMenuOpenResponse;
import com.qrmenu.restaurant.dto.PublicRestaurantResponse;
import com.qrmenu.restaurant.repository.RestaurantRepository;
import com.qrmenu.restaurant.service.PublicRestaurantService;
import com.qrmenu.storage.service.StoragePathValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicRestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MenuVersionRepository menuVersionRepository;

    @Mock
    private AnalyticsService analyticsService;

        @Mock
        private SlugHistoryRepository slugHistoryRepository;

        @Mock
        private StoragePathValidator storagePathValidator;

    @InjectMocks
    private PublicRestaurantService publicRestaurantService;

    @Test
    void shouldReturnPublicRestaurantInfoForActiveRestaurant() {
        Restaurant restaurant = Restaurant.builder()
                .id(7L)
                .company(Company.builder().id(3L).name("Acme").slug("acme").build())
                .name("Sunset Cafe")
                .slug("sunset-cafe")
                .address("Istanbul")
                .phone("123456")
                .logoPath("/logos/sunset.png")
                .status(Restaurant.RestaurantStatus.ACTIVE)
                .createdBy(User.builder().id(10L).build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(restaurantRepository.findBySlugIgnoreCase("sunset-cafe")).thenReturn(Optional.of(restaurant));
        when(menuVersionRepository.findLiveLanguagesByRestaurantId(7L)).thenReturn(java.util.List.of("tr", "en"));

        PublicRestaurantResponse response = publicRestaurantService.getPublicRestaurantInfo("sunset-cafe", "visit-1", "127.0.0.1", "JUnit");

        assertEquals("Sunset Cafe", response.getName());
        assertEquals("sunset-cafe", response.getSlug());
        assertEquals("/api/public/menus/sunset-cafe/pdf?lang=tr", response.getOpenUrl());
        assertEquals("/api/public/menus/sunset-cafe/pdf?lang=en", response.getOpenUrls().get("en"));
        assertTrue(response.getLanguages().contains("tr"));
        assertTrue(response.getLanguages().contains("en"));
    }

    @Test
    void shouldThrowWhenRestaurantIsNotActive() {
        Restaurant restaurant = Restaurant.builder()
                .id(8L)
                .company(Company.builder().id(4L).name("Acme").slug("acme").build())
                .name("Hidden Cafe")
                .slug("hidden-cafe")
                .status(Restaurant.RestaurantStatus.PENDING_APPROVAL)
                .createdBy(User.builder().id(11L).build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(restaurantRepository.findBySlugIgnoreCase("hidden-cafe")).thenReturn(Optional.of(restaurant));

        assertThrows(ResourceNotFoundException.class, () -> publicRestaurantService.getPublicRestaurantInfo("hidden-cafe", "visit-2", "127.0.0.1", "JUnit"));
    }

    @Test
    void shouldBuildOpenResponseForRequestedLanguage() {
        Restaurant restaurant = Restaurant.builder()
                .id(9L)
                .company(Company.builder().id(5L).name("Acme").slug("acme").build())
                .name("Ocean Bistro")
                .slug("ocean-bistro")
                .status(Restaurant.RestaurantStatus.ACTIVE)
                .createdBy(User.builder().id(12L).build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(restaurantRepository.findBySlugIgnoreCase("ocean-bistro")).thenReturn(Optional.of(restaurant));
        when(menuVersionRepository.findLiveByRestaurantAndLanguage(restaurant, MenuVersion.Language.EN))
                .thenReturn(Optional.of(MenuVersion.builder().id(11L).restaurant(restaurant).language(MenuVersion.Language.EN).isLive(true).status(MenuVersion.PdfStatus.LIVE).build()));

        PublicMenuOpenResponse response = publicRestaurantService.openMenu("ocean-bistro", "en", "visit-3", "127.0.0.1", "JUnit");

        assertEquals("ocean-bistro", response.getRestaurantSlug());
        assertEquals("en", response.getLanguage());
        assertEquals("/api/public/menus/ocean-bistro/pdf?lang=en", response.getPdfUrl());
    }

    @Test
    void shouldRejectUnsupportedLanguage() {
        Restaurant restaurant = Restaurant.builder()
                .id(10L)
                .company(Company.builder().id(6L).name("Acme").slug("acme").build())
                .name("Harbor Bistro")
                .slug("harbor-bistro")
                .status(Restaurant.RestaurantStatus.ACTIVE)
                .createdBy(User.builder().id(13L).build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(restaurantRepository.findBySlugIgnoreCase("harbor-bistro")).thenReturn(Optional.of(restaurant));

        assertThrows(com.qrmenu.common.exception.BadRequestException.class,
                () -> publicRestaurantService.openMenu("harbor-bistro", "pt", "visit-4", "127.0.0.1", "JUnit"));
    }
}
