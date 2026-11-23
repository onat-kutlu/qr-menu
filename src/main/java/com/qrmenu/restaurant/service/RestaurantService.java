package com.qrmenu.restaurant.service;

import com.qrmenu.audit.service.AuditService;
import com.qrmenu.auth.entity.Company;
import com.qrmenu.auth.entity.CompanyUser;
import com.qrmenu.auth.entity.Restaurant;
import com.qrmenu.auth.entity.User;
import com.qrmenu.auth.repository.CompanyRepository;
import com.qrmenu.auth.repository.CompanyUserRepository;
import com.qrmenu.auth.repository.UserRepository;
import com.qrmenu.common.exception.BadRequestException;
import com.qrmenu.common.exception.ForbiddenException;
import com.qrmenu.common.exception.ResourceNotFoundException;
import com.qrmenu.auth.entity.SlugChangeRequest;
import com.qrmenu.auth.entity.RestaurantApprovalRequest;
import com.qrmenu.auth.entity.SlugHistory;
import com.qrmenu.auth.repository.RestaurantApprovalRequestRepository;
import com.qrmenu.auth.repository.SlugHistoryRepository;
import com.qrmenu.auth.repository.RestaurantUserRepository;
import com.qrmenu.restaurant.dto.CreateRestaurantRequest;
import com.qrmenu.restaurant.dto.QrCodeResponse;
import com.qrmenu.restaurant.dto.RequestSlugChangeRequest;
import com.qrmenu.restaurant.dto.RestaurantResponse;
import com.qrmenu.restaurant.dto.SlugChangeRequestResponse;
import com.qrmenu.restaurant.repository.RestaurantRepository;
import com.qrmenu.restaurant.repository.SlugChangeRequestRepository;
import com.qrmenu.storage.service.StoragePathValidator;
import com.qrmenu.storage.service.LocalFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final CompanyRepository companyRepository;
    private final CompanyUserRepository companyUserRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final SlugChangeRequestRepository slugChangeRequestRepository;
    private final RestaurantAccessValidator restaurantAccessValidator;
    private final RestaurantApprovalRequestRepository restaurantApprovalRequestRepository;
    private final SlugHistoryRepository slugHistoryRepository;
    private final StoragePathValidator storagePathValidator;
    private final RestaurantUserRepository restaurantUserRepository;
    private final LocalFileStorageService localFileStorageService;

    public RestaurantResponse createRestaurant(CreateRestaurantRequest request, Long currentUserId) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Optional<CompanyUser> companyUser = companyUserRepository.findActiveByCompanyIdAndUserId(company.getId(), currentUserId);
        if (companyUser.isEmpty() || !companyUser.get().getRole().isCompanyAdmin() && !companyUser.get().getRole().isAdmin()) {
            throw new ForbiddenException("Only company admins can create restaurants");
        }

        if (isSlugTaken(request.getSlug())) {
            throw new BadRequestException("Slug already exists");
        }

        User creator = userRepository.findActiveById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Restaurant restaurant = Restaurant.builder()
                .company(company)
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .address(request.getAddress())
                .phone(request.getPhone())
                .status(Restaurant.RestaurantStatus.PENDING_APPROVAL)
                .monthlyUploadQuota(20)
                .monthlyUploadsUsed(0)
                .createdBy(creator)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Restaurant saved = restaurantRepository.save(restaurant);
        restaurantApprovalRequestRepository.save(RestaurantApprovalRequest.builder()
            .restaurant(saved)
            .company(company)
            .requestedBy(creator)
            .status(RestaurantApprovalRequest.ApprovalStatus.PENDING)
            .build());
        auditService.log(currentUserId, creator.getEmail(), "CREATE_RESTAURANT", "Created restaurant " + saved.getName(), "Restaurant", saved.getId(), null, null);
        return mapToResponse(saved);
    }

    public RestaurantResponse getRestaurantById(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        return mapToResponse(restaurant);
    }

    public RestaurantResponse getRestaurantById(Long restaurantId, Long currentUserId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        restaurantAccessValidator.requireRead(currentUserId, restaurant);
        return mapToResponse(restaurant);
    }

    public List<RestaurantResponse> getRestaurantsByCompany(Long companyId) {
        return restaurantRepository.findByCompanyId(companyId).stream().map(this::mapToResponse).toList();
    }

    public List<RestaurantResponse> getRestaurantsByCompany(Long companyId, Long currentUserId) {
        restaurantAccessValidator.requireCompanyRead(currentUserId, companyId);
        List<Restaurant> restaurants = restaurantRepository.findByCompanyId(companyId);
        if (companyUserRepository.findActiveByCompanyIdAndUserId(companyId, currentUserId)
            .map(membership -> membership.getRole() != null && membership.getRole().isCompanyAdmin())
            .orElse(false)) {
            return restaurants.stream().map(this::mapToResponse).toList();
        }
        java.util.Set<Long> assignedIds = restaurantUserRepository.findActiveByCompanyIdAndUserId(companyId, currentUserId).stream()
            .map(assignment -> assignment.getRestaurant().getId()).collect(java.util.stream.Collectors.toSet());
        return restaurants.stream().filter(restaurant -> assignedIds.contains(restaurant.getId()))
            .map(this::mapToResponse).toList();
    }

    public RestaurantResponse approveRestaurant(Long restaurantId, Long currentUserId) {
        User currentUser = userRepository.findActiveById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        if (!currentUser.getHighestRole().isAdmin()) {
            throw new ForbiddenException("Only admins can approve restaurants");
        }

        restaurant.setStatus(Restaurant.RestaurantStatus.ACTIVE);
        restaurant.setUpdatedAt(LocalDateTime.now());
        restaurantApprovalRequestRepository.findFirstByRestaurantIdAndStatusOrderByCreatedAtDesc(
                restaurantId, RestaurantApprovalRequest.ApprovalStatus.PENDING)
            .ifPresent(request -> {
                request.setStatus(RestaurantApprovalRequest.ApprovalStatus.APPROVED);
                request.setReviewedBy(currentUser);
                request.setReviewedAt(LocalDateTime.now());
                request.setUpdatedAt(LocalDateTime.now());
                restaurantApprovalRequestRepository.save(request);
            });
        auditService.log(currentUserId, currentUser.getEmail(), "APPROVE_RESTAURANT", "Approved restaurant " + restaurant.getName(), "Restaurant", restaurant.getId(), null, null);
        return mapToResponse(restaurantRepository.save(restaurant));
    }

    public SlugChangeRequestResponse requestSlugChange(Long restaurantId, RequestSlugChangeRequest request, Long currentUserId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        User requester = userRepository.findActiveById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<CompanyUser> companyUser = companyUserRepository.findActiveByCompanyIdAndUserId(restaurant.getCompany().getId(), currentUserId);
        if (companyUser.isEmpty() || !companyUser.get().getRole().isCompanyAdmin() && !companyUser.get().getRole().isAdmin()) {
            throw new ForbiddenException("Only company admins can request slug changes");
        }

        if (isSlugTaken(request.getRequestedSlug())) {
            throw new BadRequestException("Requested slug already exists");
        }

        SlugChangeRequest slugChangeRequest = SlugChangeRequest.builder()
                .restaurant(restaurant)
                .currentSlug(restaurant.getSlug())
                .requestedSlug(request.getRequestedSlug())
                .requestedBy(requester)
                .status(SlugChangeRequest.ApprovalStatus.PENDING)
                .build();

        SlugChangeRequest saved = slugChangeRequestRepository.save(slugChangeRequest);
        return SlugChangeRequestResponse.builder()
                .id(saved.getId())
                .restaurantId(restaurant.getId())
                .currentSlug(saved.getCurrentSlug())
                .requestedSlug(saved.getRequestedSlug())
                .status(saved.getStatus().name())
                .build();
    }

            public SlugChangeRequestResponse approveSlugChange(Long requestId, Long currentUserId) {
            User reviewer = userRepository.findActiveById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            SlugChangeRequest request = slugChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Slug change request not found"));
            if (!reviewer.getHighestRole().isAdmin()) {
                throw new ForbiddenException("Only platform admins can approve slug changes");
            }
            if (request.getStatus() != SlugChangeRequest.ApprovalStatus.PENDING) {
                throw new BadRequestException("Slug change request has already been reviewed");
            }

            Restaurant restaurant = request.getRestaurant();
            String oldSlug = restaurant.getSlug();
            restaurant.setSlug(request.getRequestedSlug());
            restaurant.setUpdatedAt(LocalDateTime.now());
            request.setStatus(SlugChangeRequest.ApprovalStatus.APPROVED);
            request.setReviewedBy(reviewer);
            request.setReviewedAt(LocalDateTime.now());
            request.setUpdatedAt(LocalDateTime.now());
            restaurantRepository.save(restaurant);
            slugChangeRequestRepository.save(request);
            slugHistoryRepository.save(SlugHistory.builder()
                .restaurant(restaurant)
                .oldSlug(oldSlug)
                .newSlug(restaurant.getSlug())
                .isCurrent(true)
                .build());

            return SlugChangeRequestResponse.builder()
                .id(request.getId())
                .restaurantId(restaurant.getId())
                .currentSlug(oldSlug)
                .requestedSlug(request.getRequestedSlug())
                .status(request.getStatus().name())
                .build();
            }

    public RestaurantResponse uploadLogo(Long restaurantId, String logoPath, Long currentUserId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        Optional<CompanyUser> companyUser = companyUserRepository.findActiveByCompanyIdAndUserId(restaurant.getCompany().getId(), currentUserId);
        if (companyUser.isEmpty() || !companyUser.get().getRole().isCompanyAdmin() && !companyUser.get().getRole().isAdmin()) {
            throw new ForbiddenException("Only company admins can upload logos");
        }

        validateLogoPath(logoPath);
        restaurant.setLogoPath(logoPath);
        restaurant.setUpdatedAt(LocalDateTime.now());
        Restaurant saved = restaurantRepository.save(restaurant);
        return mapToResponse(saved);
    }

    public RestaurantResponse uploadLogo(Long restaurantId, MultipartFile logo, Long currentUserId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        Optional<CompanyUser> companyUser = companyUserRepository.findActiveByCompanyIdAndUserId(restaurant.getCompany().getId(), currentUserId);
        if (companyUser.isEmpty() || (!companyUser.get().getRole().isCompanyAdmin() && !companyUser.get().getRole().isAdmin())) {
            throw new ForbiddenException("Only company admins can upload logos");
        }
        Path storedLogo = localFileStorageService.saveLogo(logo, restaurantId);
        restaurant.setLogoPath(storedLogo.toString());
        restaurant.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(restaurantRepository.save(restaurant));
    }

    public QrCodeResponse generateQrCode(Long restaurantId, String format) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        String normalizedFormat = format == null || format.isBlank() ? "png" : format.toLowerCase();
        if (!"png".equals(normalizedFormat) && !"svg".equals(normalizedFormat)) {
            throw new BadRequestException("Only png or svg formats are supported");
        }

        String content = "/qr/menu/" + restaurant.getSlug();

        return QrCodeResponse.builder()
                .format(normalizedFormat)
                .content(content)
                .data(normalizedFormat.equals("svg") ? generateSvgQrCode(content) : generatePngQrCode(content))
                .build();
    }

    public QrCodeResponse generateQrCode(Long restaurantId, String format, Long currentUserId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        restaurantAccessValidator.requireRead(currentUserId, restaurant);
        return generateQrCode(restaurantId, format);
    }

    public RestaurantResponse rejectRestaurantApproval(Long requestId, String reason, Long currentUserId) {
        User reviewer = userRepository.findActiveById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!reviewer.getHighestRole().isAdmin()) {
            throw new ForbiddenException("Only platform admins can reject restaurants");
        }
        RestaurantApprovalRequest request = restaurantApprovalRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant approval request not found"));
        if (request.getStatus() != RestaurantApprovalRequest.ApprovalStatus.PENDING) {
            throw new BadRequestException("Restaurant approval request has already been reviewed");
        }

        request.setStatus(RestaurantApprovalRequest.ApprovalStatus.REJECTED);
        request.setRejectionReason(reason);
        request.setReviewedBy(reviewer);
        request.setReviewedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        restaurantApprovalRequestRepository.save(request);

        Restaurant restaurant = request.getRestaurant();
        restaurant.setStatus(Restaurant.RestaurantStatus.INACTIVE);
        restaurant.setUpdatedAt(LocalDateTime.now());
        Restaurant saved = restaurantRepository.save(restaurant);
        auditService.log(currentUserId, reviewer.getEmail(), "REJECT_RESTAURANT",
                "Rejected restaurant " + restaurant.getName() + ": " + reason,
                "Restaurant", restaurant.getId(), null, null);
        return mapToResponse(saved);
    }

    public RestaurantResponse approveRestaurantByRequest(Long requestId, Long currentUserId) {
        User reviewer = userRepository.findActiveById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!reviewer.getHighestRole().isAdmin()) {
            throw new ForbiddenException("Only platform admins can approve restaurants");
        }
        RestaurantApprovalRequest request = restaurantApprovalRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant approval request not found"));
        return approveRestaurant(request.getRestaurant().getId(), currentUserId);
    }

    private String generatePngQrCode(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix matrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | IOException ex) {
            throw new BadRequestException("Unable to generate QR code");
        }
    }

    private String generateSvgQrCode(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix matrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300);

            int width = matrix.getWidth();
            int height = matrix.getHeight();
            StringBuilder svg = new StringBuilder();
            svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(width)
                    .append("\" height=\"").append(height)
                    .append("\" viewBox=\"0 0 ").append(width).append(" ").append(height)
                    .append("\" shape-rendering=\"crispEdges\">\n");

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix.get(x, y)) {
                        svg.append("<rect x=\"").append(x).append("\" y=\"").append(y)
                                .append("\" width=\"1\" height=\"1\" fill=\"black\"/>\n");
                    }
                }
            }

            svg.append("</svg>");
            return svg.toString();
        } catch (WriterException ex) {
            throw new BadRequestException("Unable to generate QR code");
        }
    }

    public boolean isSlugTaken(String slug) {
        return restaurantRepository.existsBySlugIgnoreCase(slug);
    }

    private void validateLogoPath(String logoPath) {
        if (logoPath == null || logoPath.isBlank()) {
            throw new BadRequestException("Logo path is required");
        }

        String lowerPath = logoPath.toLowerCase();
        if (!lowerPath.endsWith(".png") && !lowerPath.endsWith(".jpg") && !lowerPath.endsWith(".jpeg") && !lowerPath.endsWith(".webp")) {
            throw new BadRequestException("Only image files are allowed");
        }

        Path path = storagePathValidator.resolve(logoPath);
        try {
            if (!Files.exists(path)) {
                throw new BadRequestException("Logo file was not found");
            }
            if (Files.size(path) > 2 * 1024 * 1024) {
                throw new BadRequestException("Logo file exceeds the 2MB limit");
            }
        } catch (IOException ex) {
            throw new BadRequestException("Unable to read logo file");
        }
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .companyId(restaurant.getCompany().getId())
                .name(restaurant.getName())
                .slug(restaurant.getSlug())
                .description(restaurant.getDescription())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .logoPath(restaurant.getLogoPath())
                .status(restaurant.getStatus().name())
                .monthlyUploadQuota(restaurant.getMonthlyUploadQuota())
                .monthlyUploadsUsed(restaurant.getMonthlyUploadsUsed())
                .createdAt(restaurant.getCreatedAt())
                .updatedAt(restaurant.getUpdatedAt())
                .build();
    }
}
