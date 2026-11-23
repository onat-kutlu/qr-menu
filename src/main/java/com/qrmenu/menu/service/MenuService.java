package com.qrmenu.menu.service;

import com.qrmenu.audit.service.AuditService;
import com.qrmenu.auth.entity.MenuVersion;
import com.qrmenu.auth.entity.PdfRejectionReason;
import com.qrmenu.auth.entity.Restaurant;
import com.qrmenu.auth.entity.User;
import com.qrmenu.auth.repository.CompanyUserRepository;
import com.qrmenu.auth.repository.UserRepository;
import com.qrmenu.auth.repository.PdfRejectionReasonRepository;
import com.qrmenu.common.exception.BadRequestException;
import com.qrmenu.common.exception.ForbiddenException;
import com.qrmenu.common.exception.ResourceNotFoundException;
import com.qrmenu.menu.dto.MenuVersionResponse;
import com.qrmenu.menu.dto.RejectMenuRequest;
import com.qrmenu.menu.dto.UploadMenuRequest;
import com.qrmenu.menu.repository.MenuVersionRepository;
import com.qrmenu.restaurant.repository.RestaurantRepository;
import com.qrmenu.restaurant.service.RestaurantAccessValidator;
import com.qrmenu.restaurant.service.RestaurantPermission;
import com.qrmenu.storage.service.PdfValidationService;
import com.qrmenu.storage.service.StoragePathValidator;
import com.qrmenu.storage.service.LocalFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MenuService {

    private final MenuVersionRepository menuVersionRepository;
    private final RestaurantRepository restaurantRepository;
    private final CompanyUserRepository companyUserRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final RestaurantAccessValidator restaurantAccessValidator;
    private final PdfRejectionReasonRepository pdfRejectionReasonRepository;
    private final StoragePathValidator storagePathValidator;
    private final PdfValidationService pdfValidationService;
    private final LocalFileStorageService localFileStorageService;

    public MenuVersionResponse uploadMenu(UploadMenuRequest request, Long currentUserId) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        User uploader = userRepository.findActiveById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        restaurantAccessValidator.requirePermission(currentUserId, restaurant, RestaurantPermission.UPLOAD_MENU);

        PdfValidationService.ValidationResult validation = validatePdfUpload(request);

        if (restaurant.getMonthlyUploadsUsed() >= restaurant.getMonthlyUploadQuota()) {
            throw new BadRequestException("Monthly upload quota exceeded");
        }

        if (request.getFileHash() != null && !request.getFileHash().isBlank()
                && !validation.sha256().equalsIgnoreCase(request.getFileHash())) {
            throw new BadRequestException("File hash does not match the uploaded PDF");
        }
        if (menuVersionRepository.existsByRestaurantIdAndFileHash(restaurant.getId(), validation.sha256())) {
            throw new BadRequestException("A menu with the same file hash already exists for this restaurant");
        }

        MenuVersion.Language language = MenuVersion.Language.valueOf(request.getLanguage().toUpperCase());
        int versionNumber = getNextVersionNumber(restaurant.getId(), language);

        MenuVersion menuVersion = MenuVersion.builder()
                .restaurant(restaurant)
                .language(language)
                .filePath(request.getFilePath())
                .fileSizeBytes(validation.fileSizeBytes())
                .fileHash(validation.sha256())
                .pageCount(validation.pageCount())
                .status(MenuVersion.PdfStatus.PENDING_APPROVAL)
                .versionNumber(versionNumber)
                .isLive(false)
                .createdBy(uploader)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        MenuVersion saved = menuVersionRepository.save(menuVersion);
        restaurant.setMonthlyUploadsUsed(restaurant.getMonthlyUploadsUsed() + 1);
        restaurantRepository.save(restaurant);
        auditService.log(currentUserId, uploader.getEmail(), "UPLOAD_MENU", "Uploaded menu for restaurant " + restaurant.getName(), "MenuVersion", saved.getId(), null, null);
        return mapToResponse(saved);
    }

    public MenuVersionResponse uploadMenu(MultipartFile file, Long restaurantId, String language, Long currentUserId) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("PDF file is required");
        }
        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!originalName.endsWith(".pdf")) {
            throw new BadRequestException("Only PDF files are allowed");
        }
        Path storedPath = localFileStorageService.savePrivatePdf(file, restaurantId, language);
        try {
            return uploadMenu(UploadMenuRequest.builder()
                    .restaurantId(restaurantId)
                    .language(language)
                    .filePath(storedPath.toString())
                    .build(), currentUserId);
        } catch (RuntimeException ex) {
            try {
                java.nio.file.Files.deleteIfExists(storedPath);
            } catch (java.io.IOException ignored) {
                log.warn("Could not clean failed PDF upload: {}", storedPath);
            }
            throw ex;
        }
    }

    public List<MenuVersionResponse> getMenusForRestaurant(Long restaurantId) {
        return menuVersionRepository.findByRestaurantId(restaurantId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<MenuVersionResponse> getMenusForRestaurant(Long restaurantId, Long currentUserId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        restaurantAccessValidator.requirePermission(currentUserId, restaurant, RestaurantPermission.VIEW_MENU);
        return getMenusForRestaurant(restaurantId);
    }

    public MenuVersionResponse approveMenu(Long menuVersionId, Long currentUserId) {
        User currentUser = userRepository.findActiveById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        MenuVersion menuVersion = menuVersionRepository.findById(menuVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu version not found"));

        if (!currentUser.getHighestRole().isAdmin()) {
            throw new ForbiddenException("Only admins can approve menus");
        }

        menuVersion.setStatus(MenuVersion.PdfStatus.APPROVED);
        menuVersion.setUpdatedAt(LocalDateTime.now());
        auditService.log(currentUserId, currentUser.getEmail(), "APPROVE_MENU", "Approved menu version " + menuVersionId, "MenuVersion", menuVersionId, null, null);
        return mapToResponse(menuVersionRepository.save(menuVersion));
    }

    public MenuVersionResponse publishMenu(Long menuVersionId, Long currentUserId) {
        User currentUser = userRepository.findActiveById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        MenuVersion menuVersion = menuVersionRepository.findById(menuVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu version not found"));

        if (!currentUser.getHighestRole().isAdmin() && !companyUserRepository.isAdminInCompany(currentUserId, menuVersion.getRestaurant().getCompany().getId())) {
            throw new ForbiddenException("Only admins can publish menus");
        }

        if (menuVersion.getStatus() != MenuVersion.PdfStatus.APPROVED
                && menuVersion.getStatus() != MenuVersion.PdfStatus.LIVE) {
            throw new BadRequestException("Only approved menus can be published");
        }

        menuVersionRepository.findAllLiveByRestaurantAndLanguage(
                        menuVersion.getRestaurant(), menuVersion.getLanguage())
                .stream()
                .filter(liveMenu -> !liveMenu.getId().equals(menuVersion.getId()))
                .forEach(liveMenu -> {
                    liveMenu.setStatus(MenuVersion.PdfStatus.ARCHIVED);
                    liveMenu.setIsLive(false);
                    liveMenu.setUpdatedAt(LocalDateTime.now());
                    menuVersionRepository.save(liveMenu);
                });

        menuVersion.setStatus(MenuVersion.PdfStatus.LIVE);
        menuVersion.setIsLive(true);
        menuVersion.setUpdatedAt(LocalDateTime.now());
        auditService.log(currentUserId, currentUser.getEmail(), "PUBLISH_MENU", "Published menu version " + menuVersionId, "MenuVersion", menuVersionId, null, null);
        return mapToResponse(menuVersionRepository.save(menuVersion));
    }

    @Transactional(readOnly = true)
    public Path getPreviewPath(Long menuVersionId, Long currentUserId) {
        User currentUser = userRepository.findActiveById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!currentUser.getHighestRole().isAdmin()) {
            throw new ForbiddenException("Only platform admins can preview menus");
        }

        MenuVersion menuVersion = menuVersionRepository.findById(menuVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu version not found"));
        Path previewPath = storagePathValidator.resolve(menuVersion.getFilePath());
        if (!java.nio.file.Files.isRegularFile(previewPath)) {
            throw new ResourceNotFoundException("Menu file not found");
        }
        return previewPath;
    }

    public MenuVersionResponse rejectMenu(Long menuVersionId, RejectMenuRequest request, Long currentUserId) {
        User currentUser = userRepository.findActiveById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        MenuVersion menuVersion = menuVersionRepository.findById(menuVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu version not found"));

        if (!currentUser.getHighestRole().isAdmin()) {
            throw new ForbiddenException("Only admins can reject menus");
        }

        menuVersion.setStatus(MenuVersion.PdfStatus.REJECTED);
        PdfRejectionReason.Category category = request.getCategory();
        pdfRejectionReasonRepository.save(PdfRejectionReason.builder()
            .menuVersion(menuVersion)
            .category(category)
            .description(request.getReason())
            .rejectedBy(currentUser)
            .build());
        menuVersion.setUpdatedAt(LocalDateTime.now());
        auditService.log(currentUserId, currentUser.getEmail(), "REJECT_MENU", "Rejected menu version " + menuVersionId + ": " + request.getReason(), "MenuVersion", menuVersionId, null, null);
        return mapToResponse(menuVersionRepository.save(menuVersion));
    }

    private PdfValidationService.ValidationResult validatePdfUpload(UploadMenuRequest request) {
        if (request.getFilePath() == null || request.getFilePath().isBlank()) {
            throw new BadRequestException("File path is required");
        }
        if (!request.getFilePath().toLowerCase().endsWith(".pdf")) {
            throw new BadRequestException("Only PDF files are allowed");
        }

        Path path = storagePathValidator.resolve(request.getFilePath());
        return pdfValidationService.validate(path);
    }

    private int getNextVersionNumber(Long restaurantId, MenuVersion.Language language) {
        List<MenuVersion> versions = menuVersionRepository.findByRestaurantId(restaurantId);
        return versions.stream()
                .filter(v -> v.getLanguage() == language)
                .mapToInt(MenuVersion::getVersionNumber)
                .max()
                .orElse(0) + 1;
    }

    private MenuVersionResponse mapToResponse(MenuVersion menuVersion) {
        return MenuVersionResponse.builder()
                .id(menuVersion.getId())
                .restaurantId(menuVersion.getRestaurant().getId())
                .language(menuVersion.getLanguage().name())
                .filePath(menuVersion.getFilePath())
                .fileSizeBytes(menuVersion.getFileSizeBytes())
                .fileHash(menuVersion.getFileHash())
                .pageCount(menuVersion.getPageCount())
                .status(menuVersion.getStatus().name())
                .versionNumber(menuVersion.getVersionNumber())
                .isLive(menuVersion.getIsLive())
                .createdAt(menuVersion.getCreatedAt())
                .updatedAt(menuVersion.getUpdatedAt())
                .build();
    }
}
