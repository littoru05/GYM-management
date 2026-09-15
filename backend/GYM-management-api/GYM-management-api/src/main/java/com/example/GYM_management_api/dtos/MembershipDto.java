package com.example.GYM_management_api.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipDto {
    private String id;
    private String code;

    @NotBlank(message = "Tên gói tập không được để trống")
    @Size(max = 100, message = "Tên gói tập không được vượt quá 100 ký tự")
    private String name;

    @NotNull(message = "Số tháng không được để trống")
    @Min(value = 1, message = "Thời hạn gói tập tối thiểu là 1 tháng")
    private Integer durationMonths;

    @NotNull(message = "Giá gói tập không được để trống")
    private BigDecimal price;

    private String description;

    private List<String> benefits;

    private String status;

    private Boolean popular;
}
