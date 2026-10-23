package com.example.GYM_management_api.controllers;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.GYM_management_api.dtos.ContactLeadDto;
import com.example.GYM_management_api.services.IContactLeadService;
import com.example.GYM_management_api.utils.TestReportWatcher;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Kiểm thử tầng Controller: ContactLeadController (Tầng Controller - Khách hàng tiềm năng)")
class ContactLeadControllerTest {

    @Mock
    private IContactLeadService contactLeadService;

    @InjectMocks
    private ContactLeadController contactLeadController;

    @Test
    @DisplayName("TC-125: Gọi API POST /api/leads trả về HTTP 201 CREATED khi gửi thông tin tư vấn thành công")
    void createLead_Returns201() {
        ContactLeadDto req = ContactLeadDto.builder()
                .name("Nguyen Thi C")
                .phone("0912345678")
                .email("thic@gmail.com")
                .message("Tu van goi tap")
                .build();

        ContactLeadDto res = ContactLeadDto.builder()
                .id("1")
                .name("Nguyen Thi C")
                .phone("0912345678")
                .email("thic@gmail.com")
                .message("Tu van goi tap")
                .status("PENDING")
                .build();

        when(contactLeadService.createLead(req)).thenReturn(res);

        ResponseEntity<ContactLeadDto> response = contactLeadController.createLead(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("1", response.getBody().getId());
        assertEquals("PENDING", response.getBody().getStatus());
        verify(contactLeadService, times(1)).createLead(req);
    }
}
