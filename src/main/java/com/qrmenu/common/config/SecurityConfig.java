package com.qrmenu.common.config;

import com.qrmenu.common.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Spring Security configuration.
 *
 * Configures:
 * - JWT authentication filter
 * - CORS policies
 * - Authorization rules
 * - Password encoding
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;

    @org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://localhost}")
    private String allowedOrigins;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, ex) -> writeSecurityError(response, 401, "UNAUTHORIZED", "Authentication is required"))
                    .accessDeniedHandler((request, response, ex) -> writeSecurityError(response, 403, "FORBIDDEN", "You do not have permission to perform this action")))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/health", "/api/info").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        
                        // Auth endpoints (public)
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                        .requestMatchers("/api/auth/verify-email", "/api/auth/request-verification-code").permitAll()
                        
                        // Actuator (public in MVP, restrict in production)
                        .requestMatchers("/actuator/health").permitAll()
                        
                        // QR public endpoints
                        .requestMatchers("/qr/**").permitAll()
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());
        
        // Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    private void writeSecurityError(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"success\":false,\"status\":" + status + ",\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration() {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(rateLimitFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isBlank())
            .collect(Collectors.toList()));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
