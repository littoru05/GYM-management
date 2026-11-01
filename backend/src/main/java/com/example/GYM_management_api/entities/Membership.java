package com.example.GYM_management_api.entities;

import com.example.GYM_management_api.entities.enums.CommonStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "memberships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membership extends BaseEntity {

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "membership_benefits", joinColumns = @JoinColumn(name = "membership_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Column(name = "benefit")
    @Builder.Default
    private List<String> benefits = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CommonStatus status = CommonStatus.ACTIVE;

    @Column(name = "popular")
    @Builder.Default
    private Boolean popular = false;
}
