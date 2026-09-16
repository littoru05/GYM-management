package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.repositories.TrainerRepository;
import com.example.GYM_management_api.services.ITrainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TrainerServiceImpl implements ITrainerService {

    private final TrainerRepository trainerRepository;
}
