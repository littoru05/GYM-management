package com.example.GYM_management_api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.GYM_management_api.dtos.ContactLeadDto;
import com.example.GYM_management_api.entities.ContactLead;
import com.example.GYM_management_api.mappers.ContactLeadMapper;
import com.example.GYM_management_api.repositories.ContactLeadRepository;
import com.example.GYM_management_api.services.impl.ContactLeadServiceImpl;
import com.example.GYM_management_api.utils.TestReportWatcher;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Kiểm thử nghiệp vụ ContactLeadServiceImpl (Tầng Service - Khách hàng tiềm năng)")
class ContactLeadServiceImplTest {

    @Mock
    private ContactLeadRepository contactLeadRepository;

    @Mock
    private ContactLeadMapper contactLeadMapper;

    @InjectMocks
    private ContactLeadServiceImpl contactLeadService;

    @Test
    @DisplayName("TC-106: Tiếp nhận và lưu thông tin khách hàng tiềm năng thành công")
    void createLead_Success() {
        ContactLeadDto request = ContactLeadDto.builder()
                .name("Nguyen Thi C")
                .phone("0912345678")
                .email("thic@gmail.com")
                .message("Muốn tư vấn gói tập giảm cân")
                .build();

        ContactLead entity = ContactLead.builder()
                .name("Nguyen Thi C")
                .phone("0912345678")
                .email("thic@gmail.com")
                .message("Muốn tư vấn gói tập giảm cân")
                .build();
        entity.setId(1L);

        ContactLeadDto responseDto = ContactLeadDto.builder()
                .id("1")
                .name("Nguyen Thi C")
                .phone("0912345678")
                .email("thic@gmail.com")
                .message("Muốn tư vấn gói tập giảm cân")
                .status("PENDING")
                .build();

        when(contactLeadMapper.toEntity(request)).thenReturn(entity);
        when(contactLeadRepository.save(entity)).thenReturn(entity);
        when(contactLeadMapper.toDto(entity)).thenReturn(responseDto);

        ContactLeadDto result = contactLeadService.createLead(request);

        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("Nguyen Thi C", result.getName());
        assertEquals("PENDING", result.getStatus());
        verify(contactLeadRepository, times(1)).save(entity);
    }
}
