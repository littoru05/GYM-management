package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.ContactLeadDto;

public interface IContactLeadService {

    ContactLeadDto createLead(ContactLeadDto request);
}
