package com.example.GYM_management_api.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.example.GYM_management_api.entities.enums.SubscriptionStatus;
import com.example.GYM_management_api.utils.TestReportWatcher;

@ExtendWith(TestReportWatcher.class)
@DisplayName("Kiểm thử nghiệp vụ Domain Entity Member (Tầng Entity)")
class MemberTest {

    private Member member;
    private Membership vipMembership;
    private Membership basicMembership;

    @BeforeEach
    void setUp() {
        vipMembership = Membership.builder().name("VIP Membership").durationMonths(12).build();
        basicMembership = Membership.builder().name("Basic Membership").durationMonths(1).build();

        member = Member.builder()
                .code("MEM-001")
                .name("Nguyen Van Member")
                .subscriptions(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("TC-24: getActiveSubscription trả về null khi hội viên chưa có gói tập nào")
    void getActiveSubscription_WhenNoSubscriptions_ReturnsNull() {
        member.setSubscriptions(null);
        assertNull(member.getActiveSubscription());

        member.setSubscriptions(new ArrayList<>());
        assertNull(member.getActiveSubscription());
    }

    @Test
    @DisplayName("TC-25: getActiveSubscription trả về null khi tất cả gói tập đều đã HẾT HẠN (EXPIRED)")
    void getActiveSubscription_WhenAllExpired_ReturnsNull() {
        MemberSubscription expiredSub = MemberSubscription.builder()
                .status(SubscriptionStatus.EXPIRED)
                .endDate(LocalDate.now().minusDays(5))
                .build();
        member.getSubscriptions().add(expiredSub);

        assertNull(member.getActiveSubscription());
    }

    @Test
    @DisplayName("TC-26: getActiveSubscription trả về gói tập ACTIVE có ngày hết hạn xa nhất")
    void getActiveSubscription_WhenMultipleActive_ReturnsLatest() {
        LocalDate today = LocalDate.now();
        MemberSubscription sub1 = MemberSubscription.builder()
                .membership(basicMembership)
                .status(SubscriptionStatus.ACTIVE)
                .endDate(today.plusMonths(1))
                .build();

        MemberSubscription sub2 = MemberSubscription.builder()
                .membership(vipMembership)
                .status(SubscriptionStatus.ACTIVE)
                .endDate(today.plusMonths(6))
                .build();

        member.getSubscriptions().addAll(List.of(sub1, sub2));

        MemberSubscription activeSub = member.getActiveSubscription();
        assertNotNull(activeSub);
        assertEquals(vipMembership, activeSub.getMembership());
        assertEquals(today.plusMonths(6), activeSub.getEndDate());
    }

    @Test
    @DisplayName("TC-27: getLatestExpiryDate trả về đúng ngày hết hạn của gói tập ACTIVE")
    void getLatestExpiryDate_WithActiveSubscription() {
        LocalDate expectedExpiry = LocalDate.now().plusMonths(3);
        MemberSubscription sub = MemberSubscription.builder()
                .status(SubscriptionStatus.ACTIVE)
                .endDate(expectedExpiry)
                .build();
        member.getSubscriptions().add(sub);

        assertEquals(expectedExpiry, member.getLatestExpiryDate());
    }

    @Test
    @DisplayName("TC-28: getLatestExpiryDate lấy ngày hết hạn xa nhất từ các gói đã hết hạn khi không có gói ACTIVE")
    void getLatestExpiryDate_WithoutActiveSubscription_ReturnsMaxExpiredDate() {
        LocalDate date1 = LocalDate.now().minusDays(30);
        LocalDate date2 = LocalDate.now().minusDays(5);

        MemberSubscription sub1 = MemberSubscription.builder()
                .status(SubscriptionStatus.EXPIRED)
                .endDate(date1)
                .build();
        MemberSubscription sub2 = MemberSubscription.builder()
                .status(SubscriptionStatus.EXPIRED)
                .endDate(date2)
                .build();

        member.getSubscriptions().addAll(List.of(sub1, sub2));

        assertEquals(date2, member.getLatestExpiryDate());
    }

    @Test
    @DisplayName("TC-29: getCurrentMembership trả về gói tập đang ACTIVE hoặc gói gần nhất")
    void getCurrentMembership_Success() {
        MemberSubscription sub = MemberSubscription.builder()
                .membership(vipMembership)
                .status(SubscriptionStatus.ACTIVE)
                .endDate(LocalDate.now().plusMonths(2))
                .build();
        member.getSubscriptions().add(sub);

        assertEquals(vipMembership, member.getCurrentMembership());
    }
}

