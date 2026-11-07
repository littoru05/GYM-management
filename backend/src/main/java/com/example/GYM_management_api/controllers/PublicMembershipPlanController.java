package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.MembershipDto;
import com.example.GYM_management_api.services.IMembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Tag(name = "Gói tập công khai (Public Membership Plans)")
public class PublicMembershipPlanController {

    private final IMembershipService membershipService;

    @GetMapping({"/membership-plans", "/packages"})
    @Operation(summary = "Lấy danh sách gói tập công khai (Public) bao gồm giá, thời hạn, quyền lợi, ảnh minh họa và tag nổi bật")
    @ApiResponse(responseCode = "200", description = "Lấy danh sách gói tập thành công",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MembershipDto.class))))
    public ResponseEntity<List<MembershipDto>> getPublicMembershipPlans() {
        List<MembershipDto> activePlans = membershipService.getAllMemberships("ACTIVE");
        return ResponseEntity.ok(activePlans);
    }
}

