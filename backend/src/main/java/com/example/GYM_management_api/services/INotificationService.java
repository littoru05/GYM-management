package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.RecentContactLeadDto;
import com.example.GYM_management_api.dtos.UnreadCountResponseDto;

import java.util.List;

public interface INotificationService {
    UnreadCountResponseDto getUnreadCount();
    List<RecentContactLeadDto> getRecentContacts(int limit);
    RecentContactLeadDto markAsRead(Long id);
    int markAllAsRead();
}

