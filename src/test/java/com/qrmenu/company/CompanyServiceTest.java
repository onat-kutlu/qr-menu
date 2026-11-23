package com.qrmenu.company;

import com.qrmenu.audit.service.AuditService;
import com.qrmenu.auth.entity.Company;
import com.qrmenu.auth.entity.CompanyUser;
import com.qrmenu.auth.entity.User;
import com.qrmenu.auth.repository.CompanyRepository;
import com.qrmenu.auth.repository.CompanyUserRepository;
import com.qrmenu.auth.repository.UserRepository;
import com.qrmenu.common.enums.UserRole;
import com.qrmenu.common.exception.BadRequestException;
import com.qrmenu.company.dto.CompanyResponse;
import com.qrmenu.company.dto.CreateCompanyRequest;
import com.qrmenu.company.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyUserRepository companyUserRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CompanyService companyService;

    private User creator;

    @BeforeEach
    void setUp() {
        creator = User.builder()
                .id(1L)
                .email("creator@example.com")
                .passwordHash("hash")
                .isActive(true)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateCompanyAndAssignCompanyAdminRole() {
        CreateCompanyRequest request = CreateCompanyRequest.builder()
                .name("Test Company")
                .slug("test-company")
                .description("A test company")
                .build();

        when(companyRepository.slugExists("test-company")).thenReturn(false);
        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(creator));
        when(companyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(companyUserRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CompanyResponse response = companyService.createCompany(request, 1L);

        assertNotNull(response);
        assertEquals("Test Company", response.getName());
        assertEquals("test-company", response.getSlug());
    }

    @Test
    void shouldRejectDuplicateSlug() {
        CreateCompanyRequest request = CreateCompanyRequest.builder()
                .name("Existing Company")
                .slug("existing-company")
                .build();

        when(companyRepository.slugExists("existing-company")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> companyService.createCompany(request, 1L));
    }

    @Test
    void shouldRemoveUserFromCompanyWhenRequesterIsAdmin() {
        Company company = Company.builder()
                .id(2L)
                .name("Test Company")
                .slug("test-company")
                .status(Company.CompanyStatus.ACTIVE)
                .build();

        User member = User.builder()
                .id(2L)
                .email("member@example.com")
                .isActive(true)
                .build();

        CompanyUser currentMembership = CompanyUser.builder()
                .company(company)
                .user(creator)
                .role(UserRole.COMPANY_ADMIN)
                .isActive(true)
                .build();

        CompanyUser targetMembership = CompanyUser.builder()
                .company(company)
                .user(member)
                .role(UserRole.COMPANY_USER)
                .isActive(true)
                .build();

        when(companyRepository.findById(2L)).thenReturn(Optional.of(company));
        when(userRepository.findActiveById(2L)).thenReturn(Optional.of(member));
        when(companyUserRepository.findActiveByCompanyIdAndUserId(2L, 2L)).thenReturn(Optional.of(targetMembership));
        when(companyUserRepository.findActiveByCompanyIdAndUserId(2L, 1L)).thenReturn(Optional.of(currentMembership));

        companyService.removeUserFromCompany(2L, 2L, 1L);

        assertFalse(targetMembership.getIsActive());
        verify(companyUserRepository).save(targetMembership);
    }
}
