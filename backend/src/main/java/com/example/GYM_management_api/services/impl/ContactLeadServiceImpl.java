package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.dtos.ContactLeadDto;
import com.example.GYM_management_api.entities.ContactLead;
import com.example.GYM_management_api.mappers.ContactLeadMapper;
import com.example.GYM_management_api.repositories.ContactLeadRepository;
import com.example.GYM_management_api.services.IContactLeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ContactLeadServiceImpl implements IContactLeadService {

    private final ContactLeadRepository contactLeadRepository;
    private final ContactLeadMapper contactLeadMapper;

    @Override
    public ContactLeadDto createLead(ContactLeadDto request) {
        ContactLead entity = contactLeadMapper.toEntity(request);
        ContactLead saved = contactLeadRepository.save(entity);
        return contactLeadMapper.toDto(saved);
    }
}
