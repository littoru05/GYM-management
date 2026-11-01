package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.dtos.RecentContactLeadDto;
import com.example.GYM_management_api.dtos.UnreadCountResponseDto;
import com.example.GYM_management_api.entities.ContactLead;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.repositories.ContactLeadRepository;
import com.example.GYM_management_api.repositories.NotificationRepository;
import com.example.GYM_management_api.services.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements INotificationService {

    private final ContactLeadRepository contactLeadRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public UnreadCountResponseDto getUnreadCount() {
        long count = contactLeadRepository.countByIsReadFalse();
        return UnreadCountResponseDto.builder()
                .unreadCount(count)
                .build();
    }

    @Override
    public List<RecentContactLeadDto> getRecentContacts(int limit) {
        int safeLimit = (limit <= 0) ? 5 : Math.min(limit, 20);
        List<ContactLead> leads = contactLeadRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, safeLimit));
        return leads.stream()
                .map(this::toRecentDto)
                .toList();
    }

    @Override
    @Transactional
    public RecentContactLeadDto markAsRead(Long id) {
        ContactLead lead = contactLeadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy liên hệ với id: " + id));
        lead.setIsRead(true);
        ContactLead saved = contactLeadRepository.save(lead);
        return toRecentDto(saved);
    }

    @Override
    @Transactional
    public int markAllAsRead() {
        return contactLeadRepository.markAllAsRead();
    }

    private RecentContactLeadDto toRecentDto(ContactLead entity) {
        return RecentContactLeadDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .message(entity.getMessage())
                .createdAt(entity.getCreatedAt())
                .isRead(entity.getIsRead() != null ? entity.getIsRead() : false)
                .status(entity.getStatus() != null ? entity.getStatus().name() : "NEW")
                .build();
    }
}
