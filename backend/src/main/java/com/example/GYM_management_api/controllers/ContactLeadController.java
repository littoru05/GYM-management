package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.ContactLeadDto;
import com.example.GYM_management_api.dtos.ErrorResponseDto;
import com.example.GYM_management_api.services.IContactLeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@Tag(name = "Liên hệ (Public)")
public class ContactLeadController {

    private final IContactLeadService contactLeadService;

    @PostMapping
    @Operation(summary = "Tạo yêu cầu liên hệ / tư vấn mới (Public)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = ContactLeadDto.class))),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<ContactLeadDto> createLead(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin liên hệ tư vấn cơ bản",
                    content = @Content(
                            schema = @Schema(implementation = ContactLeadDto.class),
                            examples = @ExampleObject(
                                    name = "Thông tin cơ bản",
                                    summary = "Mẫu điền liên hệ tư vấn cơ bản",
                                    value = "{\n  \"name\": \"Nguyen Van A\",\n  \"phone\": \"0901234567\",\n  \"email\": \"testlead1@gmail.com\",\n  \"message\": \"Tôi cần tư vấn gói tập Gym 3 tháng\"\n}"
                            )
                    )
            )
            @Valid @RequestBody ContactLeadDto request) {
        ContactLeadDto created = contactLeadService.createLead(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
