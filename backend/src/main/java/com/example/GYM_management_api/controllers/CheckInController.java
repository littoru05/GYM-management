package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.CheckInDto;
import com.example.GYM_management_api.dtos.ErrorResponseDto;
import com.example.GYM_management_api.services.ICheckInService;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/check-ins")
@RequiredArgsConstructor
@Tag(name = "Điểm danh Check-in tại Quầy (Check-ins)")
@SecurityRequirement(name = "Bearer Authentication")
public class CheckInController {

    private final ICheckInService checkInService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Điểm danh check-in thành công",
                    content = @Content(schema = @Schema(implementation = CheckInDto.class))),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<CheckInDto> checkIn(@RequestBody CheckInDto request) {
        CheckInDto result = checkInService.checkIn(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách check-in hôm nay thành công",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CheckInDto.class)))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<List<CheckInDto>> getTodayCheckIns() {
        List<CheckInDto> list = checkInService.getTodayCheckIns();
        return ResponseEntity.ok(list);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy toàn bộ lịch sử check-in thành công",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CheckInDto.class)))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<List<CheckInDto>> getAllCheckIns() {
        List<CheckInDto> list = checkInService.getAllCheckIns();
        return ResponseEntity.ok(list);
    }
}
