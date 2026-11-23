package com.qrmenu.auth;

import com.qrmenu.auth.repository.CompanyUserRepository;
import com.qrmenu.auth.repository.UserRepository;
import com.qrmenu.auth.service.AuthService;
import com.qrmenu.common.exception.UnauthorizedException;
import com.qrmenu.common.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceSecurityTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CompanyUserRepository companyUserRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRejectRefreshForJwtMissingActiveRedisSession() {
        when(jwtTokenProvider.validateToken("revoked-token")).thenReturn(true);
        when(redisTemplate.hasKey("token:blacklist:revoked-token")).thenReturn(false);
        when(redisTemplate.hasKey("token:revoked-token")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken("revoked-token"));
    }
}
