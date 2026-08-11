package com.n2n.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "general_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_type", nullable = false, length = 50)
    private String settingType; // e.g., PAYMENT_METHOD

    @Column(nullable = false, length = 100)
    private String name; // e.g., Credit Card

    @Column(name = "provider_key", length = 50)
    private String providerKey; // e.g., card, fpx, grabpay

    @Column(length = 50)
    private String icon; // e.g., 💳

    @Column(length = 255)
    private String description; // e.g., Instant authorization via Visa/Mastercard

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
