package com.example.GYM_management_api.entities;

import com.example.GYM_management_api.entities.enums.CommonStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "trainers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trainer extends BaseEntity {

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "specialty", length = 150)
    private String specialty;

    @Column(name = "experience", length = 100)
    private String experience;

    @Column(name = "price_per_session", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal pricePerSession = BigDecimal.ZERO;

    @Column(name = "avatar", length = 500)
    private String avatar;

    @Column(name = "bio", length = 500)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CommonStatus status = CommonStatus.ACTIVE;
}
