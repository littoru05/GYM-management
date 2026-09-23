package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.ErrorResponseDto;
import com.example.GYM_management_api.dtos.MembershipDto;
import com.example.GYM_management_api.services.IMembershipService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
@Tag(name = "Quản lý Gói tập (Memberships)")
public class MembershipController {

    private final IMembershipService membershipService;

    @GetMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách gói tập thành công", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MembershipDto.class))))
    })
    public ResponseEntity<List<MembershipDto>> getMemberships(
            @Parameter(description = "Lọc theo trạng thái (ACTIVE, INACTIVE)", example = "ACTIVE")
            @RequestParam(required = false) String status) {
        List<MembershipDto> list = membershipService.getAllMemberships(status);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết gói tập thành công", content = @Content(schema = @Schema(implementation = MembershipDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<MembershipDto> getMembershipById(
            @Parameter(description = "ID gói tập", example = "1")
            @PathVariable Long id) {
        MembershipDto dto = membershipService.getMembershipById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = MembershipDto.class))),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<MembershipDto> createMembership(
            @Valid @RequestBody MembershipDto request) {
        MembershipDto created = membershipService.createMembership(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MembershipDto.class))),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<MembershipDto> updateMembership(
            @Parameter(description = "ID gói tập", example = "1")
            @PathVariable Long id,
            @RequestBody MembershipDto request) {
        MembershipDto updated = membershipService.updateMembership(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<?> deleteMembership(
            @Parameter(description = "ID gói tập", example = "1")
            @PathVariable Long id) {
        membershipService.deleteMembership(id);
        return ResponseEntity.ok(Map.of(
                "status", HttpStatus.OK.value(),
                "message", "Xử lý xóa gói tập thành công."
        ));
    }
}
