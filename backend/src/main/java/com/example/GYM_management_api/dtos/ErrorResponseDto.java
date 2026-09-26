package com.example.GYM_management_api.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Đối tượng phản hồi thông tin lỗi chuẩn RESTful")
public class ErrorResponseDto {

    @Schema(description = "Mã trạng thái HTTP", example = "400")
    private int status;

    @Schema(description = "Loại lỗi HTTP", example = "Bad Request")
    private String error;

    @Schema(description = "Thông báo lỗi chi tiết", example = "Dữ liệu yêu cầu không hợp lệ")
    private String message;

    @Schema(description = "Danh sách chi tiết các trường vi phạm validation")
    private Map<String, String> errors;

    @Schema(description = "Thời điểm xảy ra lỗi")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "Đường dẫn API xảy ra lỗi", example = "/api/members")
    private String path;
}
