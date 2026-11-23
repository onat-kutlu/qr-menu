package com.qrmenu.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "restaurant_users", indexes = {
        @Index(name = "idx_restaurant_users_restaurant_id", columnList = "restaurant_id"),
        @Index(name = "idx_restaurant_users_user_id", columnList = "user_id")
}, uniqueConstraints = @UniqueConstraint(name = "uk_restaurant_user", columnNames = {"restaurant_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 500)
    private String permissions;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public boolean hasPermission(String permission) {
        if (permissions == null || permissions.isBlank()) {
            return false;
        }
        return java.util.Arrays.stream(permissions.split(","))
                .map(String::trim)
                .anyMatch(permission::equalsIgnoreCase);
    }
}
