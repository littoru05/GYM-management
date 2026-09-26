package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.CheckInDto;

import java.util.List;

public interface ICheckInService {

    CheckInDto checkIn(CheckInDto request);

    List<CheckInDto> getTodayCheckIns();

    List<CheckInDto> getAllCheckIns();
}
