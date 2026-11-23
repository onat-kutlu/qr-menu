package com.qrmenu.analytics;

import com.qrmenu.analytics.entity.AnalyticsEvent;
import com.qrmenu.analytics.repository.AnalyticsEventRepository;
import com.qrmenu.analytics.service.AnalyticsService;
import com.qrmenu.auth.entity.Company;
import com.qrmenu.auth.entity.MenuVersion;
import com.qrmenu.auth.entity.Restaurant;
import com.qrmenu.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsEventRepository analyticsEventRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void shouldRecordLandingViewWithHashedIpAndVisitId() {
        Restaurant restaurant = Restaurant.builder()
                .id(12L)
                .company(Company.builder().id(7L).name("Acme").slug("acme").build())
                .name("Sunset")
                .slug("sunset")
                .status(Restaurant.RestaurantStatus.ACTIVE)
                .createdBy(User.builder().id(1L).build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        analyticsService.recordLandingView(restaurant, "visit-abc", "127.0.0.1", "JUnit");

        ArgumentCaptor<AnalyticsEvent> captor = ArgumentCaptor.forClass(AnalyticsEvent.class);
        verify(analyticsEventRepository).save(captor.capture());

        AnalyticsEvent event = captor.getValue();
        assertEquals("LANDING_VIEW", event.getEventType());
        assertEquals(12L, event.getRestaurantId());
        assertEquals(7L, event.getCompanyId());
        assertEquals("visit-abc", event.getVisitId());
        assertNotNull(event.getIpHash());
        assertEquals("JUnit", event.getUserAgent());
    }

    @Test
    void shouldRecordMenuOpenWithLanguage() {
        Restaurant restaurant = Restaurant.builder()
                .id(13L)
                .company(Company.builder().id(8L).name("Acme").slug("acme").build())
                .name("Ocean")
                .slug("ocean")
                .status(Restaurant.RestaurantStatus.ACTIVE)
                .createdBy(User.builder().id(2L).build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        analyticsService.recordMenuOpen(restaurant, MenuVersion.Language.TR, "visit-xyz", "10.0.0.2", "JUnit");

        ArgumentCaptor<AnalyticsEvent> captor = ArgumentCaptor.forClass(AnalyticsEvent.class);
        verify(analyticsEventRepository).save(captor.capture());

        AnalyticsEvent event = captor.getValue();
        assertEquals("MENU_OPEN", event.getEventType());
        assertEquals(MenuVersion.Language.TR, event.getLanguage());
        assertEquals("visit-xyz", event.getVisitId());
        assertNotNull(event.getIpHash());
    }
}
