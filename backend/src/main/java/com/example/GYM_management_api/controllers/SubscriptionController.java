package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.ErrorResponseDto;
import com.example.GYM_management_api.dtos.MemberSubscriptionDto;
import com.example.GYM_management_api.services.IMemberSubscriptionService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Quản lý Hợp đồng Gói tập (Subscriptions)")
@SecurityRequirement(name = "Bearer Authentication")
public class SubscriptionController {

    private final IMemberSubscriptionService subscriptionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Kích hoạt hoặc gia hạn gói tập thành công",
                    content = @Content(schema = @Schema(implementation = MemberSubscriptionDto.class))),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<MemberSubscriptionDto> createSubscription(@RequestBody MemberSubscriptionDto request) {
        MemberSubscriptionDto created = subscriptionService.createSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách tất cả hợp đồng thành công",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MemberSubscriptionDto.class)))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<List<MemberSubscriptionDto>> getAllSubscriptions() {
        List<MemberSubscriptionDto> list = subscriptionService.getAllSubscriptions();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/member/{memberIdOrCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy lịch sử thành công",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MemberSubscriptionDto.class)))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<List<MemberSubscriptionDto>> getSubscriptionsByMember(
            @Parameter(description = "ID hoặc Mã hội viên", example = "M0001")
            @PathVariable String memberIdOrCode) {
        List<MemberSubscriptionDto> list = subscriptionService.getSubscriptionsByMember(memberIdOrCode);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công",
                    content = @Content(schema = @Schema(implementation = MemberSubscriptionDto.class))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<MemberSubscriptionDto> getSubscriptionById(@PathVariable Long id) {
        MemberSubscriptionDto dto = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(dto);
    }
}
