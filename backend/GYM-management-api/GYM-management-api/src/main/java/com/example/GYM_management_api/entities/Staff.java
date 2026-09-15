package com.example.GYM_management_api.entities;

import com.example.GYM_management_api.entities.enums.StaffRole;
import com.example.GYM_management_api.entities.enums.StaffStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Staff extends BaseEntity {

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private StaffRole role = StaffRole.STAFF;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private StaffStatus status = StaffStatus.ACTIVE;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;
}
