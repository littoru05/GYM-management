package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.ContactLeadDto;
import com.example.GYM_management_api.exceptions.GlobalExceptionHandler;
import com.example.GYM_management_api.security.JwtRequestFilter;
import com.example.GYM_management_api.services.IContactLeadService;
import com.example.GYM_management_api.utils.TestReportWatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ContactLeadController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ExtendWith(TestReportWatcher.class)
@DisplayName("Kiểm thử tầng Controller: ContactLeadController (Tầng Controller - Khách hàng tiềm năng)")
class ContactLeadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private IContactLeadService contactLeadService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @Test
    @DisplayName("TC-125: Gọi API POST /api/leads trả về HTTP 201 CREATED khi gửi thông tin tư vấn thành công")
    void createLead_Returns201() throws Exception {
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

        when(contactLeadService.createLead(any(ContactLeadDto.class))).thenReturn(res);

        mockMvc.perform(post("/api/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(contactLeadService).createLead(any(ContactLeadDto.class));
    }
}
