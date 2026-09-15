package com.example.GYM_management_api.entity;

import com.example.GYM_management_api.entity.enums.MemberStatus;
import com.example.GYM_management_api.entity.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone", nullable = false, length = 20, unique = true)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "avatar", length = 500)
    private String avatar;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MemberSubscription> subscriptions = new ArrayList<>();

    public MemberSubscription getActiveSubscription() {
        if (subscriptions == null || subscriptions.isEmpty()) {
            return null;
        }
        return subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .max(java.util.Comparator.comparing(MemberSubscription::getEndDate))
                .orElse(null);
    }

    public LocalDate getLatestExpiryDate() {
        MemberSubscription active = getActiveSubscription();
        if (active != null) {
            return active.getEndDate();
        }
        if (subscriptions != null && !subscriptions.isEmpty()) {
            return subscriptions.stream()
                    .map(MemberSubscription::getEndDate)
                    .filter(java.util.Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .orElse(null);
        }
        return null;
    }

    public Membership getCurrentMembership() {
        MemberSubscription active = getActiveSubscription();
        if (active != null) {
            return active.getMembership();
        }
        if (subscriptions != null && !subscriptions.isEmpty()) {
            return subscriptions.stream()
                    .sorted(java.util.Comparator.comparing(MemberSubscription::getEndDate).reversed())
                    .map(MemberSubscription::getMembership)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
