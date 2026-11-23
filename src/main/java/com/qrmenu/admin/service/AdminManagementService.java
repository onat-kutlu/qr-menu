package com.qrmenu.admin.service;

import com.qrmenu.admin.dto.PageResponse;
import com.qrmenu.admin.dto.CreateAdminUserRequest;
import com.qrmenu.admin.dto.UpdateUserRoleRequest;
import com.qrmenu.admin.dto.UpdateCompanyStatusRequest;
import com.qrmenu.admin.dto.UpdateRestaurantRequest;
import com.qrmenu.audit.entity.AuditLog;
import com.qrmenu.audit.repository.AuditLogRepository;
import com.qrmenu.audit.service.AuditService;
import com.qrmenu.auth.dto.UserResponse;
import com.qrmenu.auth.entity.Company;
import com.qrmenu.auth.entity.CompanyUser;
import com.qrmenu.auth.entity.MenuVersion;
import com.qrmenu.auth.entity.Restaurant;
import com.qrmenu.auth.entity.SlugChangeRequest;
import com.qrmenu.auth.entity.User;
import com.qrmenu.auth.repository.CompanyRepository;
import com.qrmenu.auth.repository.CompanyUserRepository;
import com.qrmenu.auth.repository.UserRepository;
import com.qrmenu.auth.repository.RestaurantApprovalRequestRepository;
import com.qrmenu.common.exception.BadRequestException;
import com.qrmenu.common.exception.ResourceNotFoundException;
import com.qrmenu.company.dto.CompanyResponse;
import com.qrmenu.menu.dto.MenuVersionResponse;
import com.qrmenu.menu.repository.MenuVersionRepository;
import com.qrmenu.restaurant.dto.RestaurantResponse;
import com.qrmenu.restaurant.repository.RestaurantRepository;
import com.qrmenu.restaurant.repository.SlugChangeRequestRepository;
import com.qrmenu.admin.dto.SlugChangeRequestResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminManagementService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyUserRepository companyUserRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuVersionRepository menuVersionRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;
    private final RestaurantApprovalRequestRepository restaurantApprovalRequestRepository;
    private final SlugChangeRequestRepository slugChangeRequestRepository;

    @Transactional(readOnly = true)
    public PageResponse<CompanyResponse> listCompanies(String status, String search, int page, int size, String sort) {
        Company.CompanyStatus companyStatus = parseEnum(status, Company.CompanyStatus.class, "company status");
        var pageRequest = pageable(page, size, sort, "name");
        String normalizedSearch = blankToNull(search);
        Page<Company> companies;
        if (companyStatus == null && normalizedSearch == null) {
            companies = companyRepository.search(pageRequest);
        } else if (companyStatus == null) {
            companies = companyRepository.searchByTerm(pageRequest, normalizedSearch);
        } else if (normalizedSearch == null) {
            companies = companyRepository.searchByStatus(pageRequest, companyStatus);
        } else {
            companies = companyRepository.searchByStatusAndTerm(pageRequest, companyStatus, normalizedSearch);
        }
        Page<CompanyResponse> result = companies
                .map(this::mapCompany);
        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompany(Long companyId) {
        return mapCompany(companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found")));
    }

    public CompanyResponse updateCompanyStatus(Long companyId, UpdateCompanyStatusRequest request, Long currentUserId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        Company.CompanyStatus status = parseEnum(request.getStatus(), Company.CompanyStatus.class, "company status");
        String previousStatus = company.getStatus().name();
        company.setStatus(status);
        company.setUpdatedAt(LocalDateTime.now());
        Company saved = companyRepository.save(company);
        auditService.log(currentUserId, actorEmail(currentUserId), "UPDATE_COMPANY_STATUS",
                "Updated company status from " + previousStatus + " to " + status,
                "Company", companyId, null, null);
        return mapCompany(saved);
    }

    public CompanyResponse deactivateCompany(Long companyId, Long currentUserId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        company.softDelete();
        company.setUpdatedAt(LocalDateTime.now());
        Company saved = companyRepository.save(company);
        auditService.log(currentUserId, actorEmail(currentUserId), "DEACTIVATE_COMPANY",
                "Deactivated company " + company.getName(), "Company", companyId, null, null);
        return mapCompany(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(Boolean active, String search, int page, int size, String sort) {
        return PageResponse.from(userRepository.findAll(userSpecification(active, blankToNull(search)), pageable(page, size, sort, "createdAt"))
                .map(this::mapUser));
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        return mapUser(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

        public UserResponse createUser(CreateAdminUserRequest request, Long currentUserId) {
        if (userRepository.emailExists(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        Company company = companyRepository.findById(request.getCompanyId())
            .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        User user = User.builder().email(request.getEmail()).passwordHash(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName()).lastName(request.getLastName()).phone(request.getPhone())
            .isActive(true).emailVerified(false).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        User saved = userRepository.save(user);
        companyUserRepository.save(CompanyUser.builder().company(company).user(saved).role(request.getRole())
            .isActive(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build());
        auditService.log(currentUserId, actorEmail(currentUserId), "CREATE_ADMIN_USER",
            "Created user " + saved.getEmail(), "User", saved.getId(), null, null);
        return mapUser(saved);
        }

        public UserResponse updateUserRole(Long userId, UpdateUserRoleRequest request, Long currentUserId) {
        CompanyUser membership = companyUserRepository.findActiveByCompanyIdAndUserId(request.getCompanyId(), userId)
            .orElseThrow(() -> new ResourceNotFoundException("User is not associated with this company"));
        membership.setRole(request.getRole());
        membership.setUpdatedAt(LocalDateTime.now());
        companyUserRepository.save(membership);
        auditService.log(currentUserId, actorEmail(currentUserId), "UPDATE_USER_ROLE",
            "Updated role for user " + userId, "User", userId, null, null);
        return getUser(userId);
        }

    public UserResponse deactivateUser(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        companyUserRepository.findActiveByUserId(userId).forEach(membership -> {
            membership.setIsActive(false);
            membership.setUpdatedAt(LocalDateTime.now());
            companyUserRepository.save(membership);
        });
        User saved = userRepository.save(user);
        auditService.log(currentUserId, actorEmail(currentUserId), "DEACTIVATE_USER",
                "Deactivated user " + user.getEmail(), "User", userId, null, null);
        return mapUser(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<RestaurantResponse> listRestaurants(Long companyId, String status, String search,
                                                             int page, int size, String sort) {
        Restaurant.RestaurantStatus restaurantStatus = parseEnum(status, Restaurant.RestaurantStatus.class, "restaurant status");
        return PageResponse.from(restaurantRepository.findAll(restaurantSpecification(companyId, restaurantStatus,
                blankToNull(search)), pageable(page, size, sort, "createdAt"))
                .map(this::mapRestaurant));
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurant(Long restaurantId) {
        return mapRestaurant(restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found")));
    }

    public RestaurantResponse updateRestaurant(Long restaurantId, UpdateRestaurantRequest request, Long currentUserId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            restaurant.setStatus(parseEnum(request.getStatus(), Restaurant.RestaurantStatus.class, "restaurant status"));
        }
        if (request.getMonthlyUploadQuota() != null) {
            restaurant.setMonthlyUploadQuota(request.getMonthlyUploadQuota());
        }
        restaurant.setUpdatedAt(LocalDateTime.now());
        Restaurant saved = restaurantRepository.save(restaurant);
        auditService.log(currentUserId, actorEmail(currentUserId), "UPDATE_RESTAURANT",
                "Updated restaurant " + restaurant.getName(), "Restaurant", restaurantId, null, null);
        return mapRestaurant(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<MenuVersionResponse> listMenus(String status, Long restaurantId, int page, int size, String sort) {
        MenuVersion.PdfStatus menuStatus = parseEnum(status, MenuVersion.PdfStatus.class, "menu status");
        return PageResponse.from(menuVersionRepository.findAll(menuSpecification(menuStatus, restaurantId),
                pageable(page, size, sort, "createdAt"))
                .map(this::mapMenu));
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLog> listAuditLogs(String action, String status, Long userId, int page, int size, String sort) {
        return PageResponse.from(auditLogRepository.findAll(auditLogSpecification(blankToNull(action), blankToNull(status), userId),
            pageable(page, size, sort, "createdAt")));
    }

    @Transactional(readOnly = true)
    public PageResponse<RestaurantResponse> listRestaurantApprovals(int page, int size) {
        return PageResponse.from(restaurantApprovalRequestRepository.findPending(pageable(page, size, "createdAt", "createdAt"))
                .map(request -> mapRestaurant(request.getRestaurant())));
    }

    @Transactional(readOnly = true)
    public PageResponse<SlugChangeRequestResponse> listSlugChanges(int page, int size) {
        return PageResponse.from(slugChangeRequestRepository.findByStatus(
            SlugChangeRequest.ApprovalStatus.PENDING, pageable(page, size, "createdAt", "createdAt"))
            .map(this::mapSlugChange));
    }

    private Pageable pageable(int page, int size, String sort, String defaultSort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String property = switch (sort == null ? "" : sort) {
            case "name", "slug", "createdAt", "updatedAt", "status", "email" -> sort;
            default -> defaultSort;
        };
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, property));
    }

    private Specification<User> userSpecification(Boolean active, String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), active));
            }
            if (search != null) {
                String pattern = "%" + search.toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), pattern)));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<Restaurant> restaurantSpecification(Long companyId, Restaurant.RestaurantStatus status,
                                                               String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
            if (companyId != null) {
                predicates.add(criteriaBuilder.equal(root.get("company").get("id"), companyId));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (search != null) {
                String pattern = "%" + search.toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("slug")), pattern)));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<MenuVersion> menuSpecification(MenuVersion.PdfStatus status, Long restaurantId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (restaurantId != null) {
                predicates.add(criteriaBuilder.equal(root.get("restaurant").get("id"), restaurantId));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<AuditLog> auditLogSpecification(String action, String status, Long userId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (action != null) {
                predicates.add(criteriaBuilder.equal(root.get("action"), action));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private <T extends Enum<T>> T parseEnum(String value, Class<T> type, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid " + field + ": " + value);
        }
    }

    private String actorEmail(Long userId) {
        return userRepository.findById(userId).map(User::getEmail).orElse("unknown");
    }

    private CompanyResponse mapCompany(Company company) {
        return CompanyResponse.builder().id(company.getId()).name(company.getName()).slug(company.getSlug())
                .description(company.getDescription()).status(company.getStatus().name())
                .createdAt(company.getCreatedAt()).updatedAt(company.getUpdatedAt()).build();
    }

    private UserResponse mapUser(User user) {
        return UserResponse.builder().id(user.getId()).email(user.getEmail()).firstName(user.getFirstName())
                .lastName(user.getLastName()).fullName(user.getFullName()).phone(user.getPhone())
                .globalRole(user.getGlobalRole())
                .isActive(user.getIsActive()).emailVerified(user.getEmailVerified())
                .companies(companyUserRepository.findActiveByUserId(user.getId()).stream()
                        .map(membership -> new UserResponse.CompanyInfo(membership.getCompany().getId(),
                                membership.getCompany().getName(), membership.getRole().name())).toList())
                .lastLoginAt(user.getLastLoginAt()).createdAt(user.getCreatedAt()).build();
    }

    private RestaurantResponse mapRestaurant(Restaurant restaurant) {
        return RestaurantResponse.builder().id(restaurant.getId()).companyId(restaurant.getCompany().getId())
                .name(restaurant.getName()).slug(restaurant.getSlug()).description(restaurant.getDescription())
                .address(restaurant.getAddress()).phone(restaurant.getPhone()).logoPath(restaurant.getLogoPath())
                .status(restaurant.getStatus().name()).monthlyUploadQuota(restaurant.getMonthlyUploadQuota())
                .monthlyUploadsUsed(restaurant.getMonthlyUploadsUsed()).createdAt(restaurant.getCreatedAt())
                .updatedAt(restaurant.getUpdatedAt()).build();
    }

    private MenuVersionResponse mapMenu(MenuVersion menu) {
        return MenuVersionResponse.builder().id(menu.getId()).restaurantId(menu.getRestaurant().getId())
                .language(menu.getLanguage().name()).filePath(menu.getFilePath()).fileSizeBytes(menu.getFileSizeBytes())
                .fileHash(menu.getFileHash()).pageCount(menu.getPageCount()).status(menu.getStatus().name())
                .versionNumber(menu.getVersionNumber()).isLive(menu.getIsLive()).createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt()).build();
    }

    private SlugChangeRequestResponse mapSlugChange(SlugChangeRequest request) {
        return SlugChangeRequestResponse.builder().id(request.getId())
                .restaurantId(request.getRestaurant().getId()).currentSlug(request.getCurrentSlug())
                .requestedSlug(request.getRequestedSlug()).status(request.getStatus().name())
                .createdAt(request.getCreatedAt()).build();
    }
}
