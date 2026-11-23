package com.qrmenu.common.config;

import com.qrmenu.common.response.ApiResponse;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthCheckController {

    private final HealthEndpoint healthEndpoint;
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        String status = healthEndpoint.health().getStatus().getCode();
        HttpStatus httpStatus = Status.UP.getCode().equals(status) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(httpStatus)
            .body(ApiResponse.ok(Map.of("status", status),
                Status.UP.getCode().equals(status) ? "Application is healthy" : "Application is unavailable"));
    }
    
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, String>>> info() {
        return ResponseEntity.ok(
                ApiResponse.ok(Map.of(
                        "name", "QR Menu",
                        "version", "0.0.1-MVP"
                ), "Application info")
        );
    }
}
