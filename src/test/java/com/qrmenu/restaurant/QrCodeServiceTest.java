package com.qrmenu.restaurant;

import com.qrmenu.auth.entity.Company;
import com.qrmenu.auth.entity.Restaurant;
import com.qrmenu.auth.entity.User;
import com.qrmenu.auth.repository.CompanyRepository;
import com.qrmenu.auth.repository.CompanyUserRepository;
import com.qrmenu.auth.repository.UserRepository;
import com.qrmenu.audit.service.AuditService;
import com.qrmenu.restaurant.dto.QrCodeResponse;
import com.qrmenu.restaurant.repository.RestaurantRepository;
import com.qrmenu.restaurant.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QrCodeServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyUserRepository companyUserRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        restaurant = Restaurant.builder()
                .id(55L)
                .company(Company.builder().id(5L).name("Acme").slug("acme").build())
                .name("Sunset Cafe")
                .slug("sunset-cafe")
                .status(Restaurant.RestaurantStatus.ACTIVE)
                .createdBy(User.builder().id(10L).build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldGeneratePngQrCodeForRestaurant() {
        when(restaurantRepository.findById(55L)).thenReturn(Optional.of(restaurant));

        QrCodeResponse response = restaurantService.generateQrCode(55L, "png");

        assertNotNull(response);
        assertEquals("png", response.getFormat());
        assertEquals("/qr/menu/sunset-cafe", response.getContent());
        assertTrue(response.getData().startsWith("data:image/png;base64,"));
        byte[] decoded = Base64.getDecoder().decode(response.getData().replace("data:image/png;base64,", ""));
        assertTrue(decoded.length > 0);
    }

    @Test
    void shouldGenerateSvgQrCodeForRestaurant() {
        when(restaurantRepository.findById(55L)).thenReturn(Optional.of(restaurant));

        QrCodeResponse response = restaurantService.generateQrCode(55L, "svg");

        assertNotNull(response);
        assertEquals("svg", response.getFormat());
        assertEquals("/qr/menu/sunset-cafe", response.getContent());
        assertTrue(response.getData().startsWith("<svg"));
        assertTrue(response.getData().contains("viewBox"));
    }
}
