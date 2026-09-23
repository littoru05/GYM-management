package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.ErrorResponseDto;
import com.example.GYM_management_api.dtos.MemberDto;
import com.example.GYM_management_api.services.IMemberService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Quản lý Hội viên")
@SecurityRequirement(name = "Bearer Authentication")
public class MemberController {

    private final IMemberService memberService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<Page<MemberDto>> getMembers(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng phần tử mỗi trang", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Từ khóa tìm kiếm (tên, SĐT, mã hội viên)", example = "Nguyễn")
            @RequestParam(required = false) String keyword) {
        Page<MemberDto> result = memberService.getMembers(page, size, keyword);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MemberDto.class))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<MemberDto> getMemberById(
            @Parameter(description = "ID hoặc Mã hội viên", example = "1")
            @PathVariable String id) {
        MemberDto dto = memberService.getMemberByIdOrCode(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = MemberDto.class))),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<MemberDto> createMember(
            @Valid @RequestBody MemberDto request) {
        MemberDto created = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MemberDto.class))),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<MemberDto> updateMember(
            @Parameter(description = "ID hội viên", example = "1")
            @PathVariable Long id,
            @RequestBody MemberDto request) {
        MemberDto updated = memberService.updateMember(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xóa hội viên thành công"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<?> deleteMember(
            @Parameter(description = "ID hội viên", example = "1")
            @PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(java.util.Map.of(
                "status", HttpStatus.OK.value(),
                "message", "Xử lý xóa hội viên thành công."
        ));
    }
}