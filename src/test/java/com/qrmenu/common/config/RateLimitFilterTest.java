package com.qrmenu.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RateLimitFilter rateLimitFilter;

    @Test
    void shouldReturnTooManyRequestsWhenLimitExceeded() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/public/menus/sunset-cafe");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {
            throw new AssertionError("filter chain should not be called when request is throttled");
        };

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(61L);

        rateLimitFilter.doFilter(request, response, filterChain);

        assertEquals(429, response.getStatus());
    }

    @Test
    void shouldAllowRequestWhenUnderLimit() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/public/menus/sunset-cafe");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> { };

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(5L);

        rateLimitFilter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(valueOperations).increment(anyString());
    }

    @Test
    void shouldUseForwardedClientIpForRateLimitKey() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/public/menus/sunset-cafe");
        request.setRemoteAddr("10.0.0.5");
        request.addHeader("X-Real-IP", "203.0.113.42");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> { };

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        rateLimitFilter.doFilter(request, response, filterChain);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).increment(keyCaptor.capture());
        assertTrue(keyCaptor.getValue().startsWith("rate-limit:203.0.113.42:"));
    }

    @Test
    void shouldIgnoreVisitIdWhenBuildingPdfRateLimitKey() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/public/menus/sunset-cafe/pdf");
        request.setRemoteAddr("127.0.0.1");
        request.addParameter("lang", "tr");
        request.setQueryString("lang=tr&visitId=random-value");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> { };

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        rateLimitFilter.doFilter(request, response, filterChain);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).increment(keyCaptor.capture());
        assertTrue(keyCaptor.getValue().contains("/pdf:lang=tr"));
        assertTrue(!keyCaptor.getValue().contains("random-value"));
    }

    @Test
    void shouldNotRateLimitPrivateEndpoints() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/dashboard");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> { };

        rateLimitFilter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verifyNoInteractions(redisTemplate, valueOperations);
    }
}
