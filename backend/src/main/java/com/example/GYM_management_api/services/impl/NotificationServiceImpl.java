package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.repositories.NotificationRepository;
import com.example.GYM_management_api.services.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;
}
