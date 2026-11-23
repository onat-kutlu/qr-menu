package com.qrmenu.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import com.qrmenu.auth.entity.Company;
import com.qrmenu.auth.entity.CompanyUser;
import com.qrmenu.auth.entity.User;
import com.qrmenu.auth.repository.CompanyUserRepository;
import com.qrmenu.auth.repository.UserRepository;
import com.qrmenu.common.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyUserRepository companyUserRepository;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldUseUserIdAsAuthenticationPrincipal() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/dashboard");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> { };

        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken("valid-token")).thenReturn("admin@example.com");
        when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn(42L);
        when(redisTemplate.hasKey("token:blacklist:valid-token")).thenReturn(false);
        when(redisTemplate.hasKey("token:valid-token")).thenReturn(true);
        when(userRepository.findActiveById(42L)).thenReturn(Optional.of(User.builder().id(42L).isActive(true).build()));
        when(companyUserRepository.findActiveByUserId(42L)).thenReturn(List.of(CompanyUser.builder()
            .user(User.builder().id(42L).build())
            .company(Company.builder().id(1L).build())
            .role(UserRole.ADMIN).isActive(true).build()));

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("42", authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
    }

    @Test
    void shouldRejectTokenWithoutRoleClaim() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/dashboard");
        request.addHeader("Authorization", "Bearer roleless-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> { };

        when(jwtTokenProvider.validateToken("roleless-token")).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken("roleless-token")).thenReturn("user@example.com");
        when(jwtTokenProvider.getUserIdFromToken("roleless-token")).thenReturn(null);

        filter.doFilter(request, response, chain);

        assertEquals(null, SecurityContextHolder.getContext().getAuthentication());
    }
}
