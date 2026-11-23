package com.qrmenu.menu;

import com.qrmenu.audit.service.AuditService;
import com.qrmenu.auth.entity.Company;
import com.qrmenu.auth.entity.MenuVersion;
import com.qrmenu.auth.entity.Restaurant;
import com.qrmenu.auth.entity.User;
import com.qrmenu.auth.repository.CompanyUserRepository;
import com.qrmenu.auth.repository.PdfRejectionReasonRepository;
import com.qrmenu.auth.repository.UserRepository;
import com.qrmenu.common.exception.BadRequestException;
import com.qrmenu.common.exception.ForbiddenException;
import com.qrmenu.menu.dto.MenuVersionResponse;
import com.qrmenu.menu.dto.UploadMenuRequest;
import com.qrmenu.menu.repository.MenuVersionRepository;
import com.qrmenu.menu.service.MenuService;
import com.qrmenu.restaurant.repository.RestaurantRepository;
import com.qrmenu.restaurant.service.RestaurantAccessValidator;
import com.qrmenu.storage.service.PdfValidationService;
import com.qrmenu.storage.service.StoragePathValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuVersionRepository menuVersionRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private CompanyUserRepository companyUserRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

        @Mock
        private PdfRejectionReasonRepository pdfRejectionReasonRepository;

        @Mock
        private RestaurantAccessValidator restaurantAccessValidator;

        @Mock
        private StoragePathValidator storagePathValidator;

        @Mock
        private PdfValidationService pdfValidationService;

        @BeforeEach
        void configureStorageMocks() {
                org.mockito.Mockito.lenient().when(storagePathValidator.resolve(any(String.class)))
                        .thenAnswer(invocation -> Path.of((String) invocation.getArgument(0)));
        }

    @InjectMocks
    private MenuService menuService;

    private User admin;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        admin = User.builder()
                .id(1L)
                .email("admin@example.com")
                .isActive(true)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        restaurant = Restaurant.builder()
                .id(10L)
                .company(Company.builder().id(3L).name("Acme").slug("acme").build())
                .name("Sunset Cafe")
                .slug("sunset-cafe")
                .status(Restaurant.RestaurantStatus.ACTIVE)
                .createdBy(admin)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldUploadMenuForCompanyAdmin() throws Exception {
        Path tempFile = Files.createTempFile("menu", ".pdf");
        Files.writeString(tempFile, "%PDF-1.4\n", StandardCharsets.UTF_8);

        UploadMenuRequest request = UploadMenuRequest.builder()
                .restaurantId(10L)
                .language("tr")
                .filePath(tempFile.toString())
                .fileSizeBytes(2048L)
                .fileHash("abc123")
                .pageCount(3)
                .build();

        when(restaurantRepository.findById(10L)).thenReturn(Optional.of(restaurant));
        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(admin));
        when(pdfValidationService.validate(any())).thenReturn(new PdfValidationService.ValidationResult(2048L, 3, "abc123"));
        when(menuVersionRepository.findByRestaurantId(10L)).thenReturn(java.util.List.of());
        when(menuVersionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MenuVersionResponse response = menuService.uploadMenu(request, 1L);

        assertEquals("PENDING_APPROVAL", response.getStatus());
        assertEquals(1, response.getVersionNumber());
    }

    @Test
    void shouldRejectNonAdminUpload() {
        UploadMenuRequest request = UploadMenuRequest.builder()
                .restaurantId(10L)
                .language("en")
                .filePath("/storage/menus/private/10/en/1/menu.pdf")
                .fileSizeBytes(2048L)
                .fileHash("def456")
                .pageCount(2)
                .build();

        when(restaurantRepository.findById(10L)).thenReturn(Optional.of(restaurant));
        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(admin));
        org.mockito.Mockito.doThrow(new com.qrmenu.common.exception.ForbiddenException("permission required"))
                .when(restaurantAccessValidator).requirePermission(any(), any(), any());

        assertThrows(ForbiddenException.class, () -> menuService.uploadMenu(request, 1L));
    }

    @Test
    void shouldRejectDuplicatePdfHash() throws Exception {
        Path tempFile = Files.createTempFile("menu", ".pdf");
        Files.writeString(tempFile, "%PDF-1.4\n", StandardCharsets.UTF_8);

        UploadMenuRequest request = UploadMenuRequest.builder()
                .restaurantId(10L)
                .language("tr")
                .filePath(tempFile.toString())
                .fileSizeBytes(2048L)
                .fileHash("duplicate-hash")
                .pageCount(1)
                .build();

        when(restaurantRepository.findById(10L)).thenReturn(Optional.of(restaurant));
        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(admin));
        when(pdfValidationService.validate(any())).thenReturn(new PdfValidationService.ValidationResult(2048L, 1, "duplicate-hash"));
        when(menuVersionRepository.existsByRestaurantIdAndFileHash(10L, "duplicate-hash")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> menuService.uploadMenu(request, 1L));
    }

    @Test
    void shouldRejectWhenQuotaExceeded() throws Exception {
        Path tempFile = Files.createTempFile("menu", ".pdf");
        Files.writeString(tempFile, "%PDF-1.4\n", StandardCharsets.UTF_8);

        restaurant.setMonthlyUploadQuota(1);
        restaurant.setMonthlyUploadsUsed(1);

        UploadMenuRequest request = UploadMenuRequest.builder()
                .restaurantId(10L)
                .language("en")
                .filePath(tempFile.toString())
                .fileSizeBytes(2048L)
                .fileHash("quota-hash")
                .pageCount(1)
                .build();

        when(restaurantRepository.findById(10L)).thenReturn(Optional.of(restaurant));
        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(admin));
        when(pdfValidationService.validate(any())).thenReturn(new PdfValidationService.ValidationResult(2048L, 1, "quota-hash"));
        assertThrows(BadRequestException.class, () -> menuService.uploadMenu(request, 1L));
    }

        @Test
        void shouldArchivePreviousLiveMenuWhenPublishingApprovedMenu() {
                MenuVersion previousLive = MenuVersion.builder()
                                .id(20L)
                                .restaurant(restaurant)
                                .language(MenuVersion.Language.TR)
                                .status(MenuVersion.PdfStatus.LIVE)
                                .isLive(true)
                                .versionNumber(1)
                                .createdBy(admin)
                                .build();
                MenuVersion nextMenu = MenuVersion.builder()
                                .id(21L)
                                .restaurant(restaurant)
                                .language(MenuVersion.Language.TR)
                                .status(MenuVersion.PdfStatus.APPROVED)
                                .isLive(false)
                                .versionNumber(2)
                                .createdBy(admin)
                                .build();

                when(userRepository.findActiveById(1L)).thenReturn(Optional.of(admin));
                when(companyUserRepository.isAdminInCompany(1L, 3L)).thenReturn(true);
                when(menuVersionRepository.findById(21L)).thenReturn(Optional.of(nextMenu));
                when(menuVersionRepository.findAllLiveByRestaurantAndLanguage(restaurant, MenuVersion.Language.TR))
                                .thenReturn(java.util.List.of(previousLive));
                when(menuVersionRepository.save(any(MenuVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

                MenuVersionResponse response = menuService.publishMenu(21L, 1L);

                assertEquals(MenuVersion.PdfStatus.ARCHIVED, previousLive.getStatus());
                assertEquals(false, previousLive.getIsLive());
                assertEquals("LIVE", response.getStatus());
                assertEquals(true, nextMenu.getIsLive());
        }

        @Test
        void shouldRejectPublishingUnapprovedMenu() {
                MenuVersion pendingMenu = MenuVersion.builder()
                                .id(22L)
                                .restaurant(restaurant)
                                .language(MenuVersion.Language.EN)
                                .status(MenuVersion.PdfStatus.PENDING_APPROVAL)
                                .isLive(false)
                                .versionNumber(1)
                                .createdBy(admin)
                                .build();

                when(userRepository.findActiveById(1L)).thenReturn(Optional.of(admin));
                when(companyUserRepository.isAdminInCompany(1L, 3L)).thenReturn(true);
                when(menuVersionRepository.findById(22L)).thenReturn(Optional.of(pendingMenu));

                assertThrows(BadRequestException.class, () -> menuService.publishMenu(22L, 1L));
        }
}
