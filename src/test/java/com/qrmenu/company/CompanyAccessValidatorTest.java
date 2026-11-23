package com.qrmenu.company;

import com.qrmenu.auth.entity.Company;
import com.qrmenu.auth.entity.CompanyUser;
import com.qrmenu.auth.entity.User;
import com.qrmenu.auth.repository.CompanyUserRepository;
import com.qrmenu.auth.repository.UserRepository;
import com.qrmenu.common.enums.UserRole;
import com.qrmenu.common.exception.ForbiddenException;
import com.qrmenu.company.service.CompanyAccessValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyAccessValidatorTest {

    @Mock
    private CompanyUserRepository companyUserRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CompanyAccessValidator validator;

    @Test
    void shouldAllowCompanyMemberToReadOwnCompany() {
        when(companyUserRepository.findActiveByUserId(7L)).thenReturn(List.of(
                membership(7L, 11L, UserRole.COMPANY_ADMIN)
        ));
        when(companyUserRepository.findActiveByCompanyIdAndUserId(11L, 7L))
                .thenReturn(Optional.of(membership(7L, 11L, UserRole.COMPANY_ADMIN)));

        assertDoesNotThrow(() -> validator.requireCompanyRead(7L, 11L));
    }

    @Test
    void shouldRejectCompanyMemberFromReadingAnotherCompany() {
        when(companyUserRepository.findActiveByUserId(7L)).thenReturn(List.of(
                membership(7L, 11L, UserRole.COMPANY_ADMIN)
        ));
        when(companyUserRepository.findActiveByCompanyIdAndUserId(22L, 7L))
                .thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> validator.requireCompanyRead(7L, 22L));
    }

    @Test
    void shouldAllowPlatformAdminToReadAnyCompany() {
        when(companyUserRepository.findActiveByUserId(1L)).thenReturn(List.of(
                membership(1L, 11L, UserRole.ADMIN)
        ));

        assertDoesNotThrow(() -> validator.requireCompanyRead(1L, 999L));
    }

    private CompanyUser membership(Long userId, Long companyId, UserRole role) {
        User user = User.builder().id(userId).email("user@example.com").isActive(true).build();
        Company company = Company.builder().id(companyId).name("Company " + companyId).slug("company-" + companyId).build();
        return CompanyUser.builder().user(user).company(company).role(role).isActive(true).build();
    }
}
