package com.qrmenu.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "slug_history", indexes = {
        @Index(name = "idx_slug_history_restaurant_id", columnList = "restaurant_id"),
        @Index(name = "idx_slug_history_new_slug", columnList = "new_slug")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlugHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "old_slug", length = 100)
    private String oldSlug;

    @Column(name = "new_slug", nullable = false, length = 100)
    private String newSlug;

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private Boolean isCurrent = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
