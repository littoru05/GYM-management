package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.exceptions.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "Tải lên tệp tin (File Upload)", description = "APIs tải lên hình ảnh sản phẩm và tài liệu phòng gym")
@SecurityRequirement(name = "Bearer Authentication")
public class FileUploadController {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp", "gif", "bmp");
    private static final Path UPLOAD_DIR = Paths.get("uploads", "products");

    @PostMapping(value = { "/upload/image", "/admin/upload/image" }, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Tải lên hình ảnh sản phẩm", description = "Nhận file ảnh từ form-data, lưu trữ an toàn và trả về URL xem trước/truy cập trực tiếp.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tải lên thành công, trả về URL truy cập ảnh"),
            @ApiResponse(responseCode = "400", description = "File rỗng, sai định dạng hoặc vượt quá 10MB"),
            @ApiResponse(responseCode = "403", description = "Không có quyền thực hiện")
    })
    public ResponseEntity<Map<String, Object>> uploadImage(
            @Parameter(description = "File ảnh sản phẩm (PNG, JPG, JPEG, WEBP, GIF)", required = true)
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn một file ảnh hợp lệ.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Dung lượng file ảnh không được vượt quá 10MB.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        String extension = "";
        int extIndex = originalFilename.lastIndexOf('.');
        if (extIndex > 0) {
            extension = originalFilename.substring(extIndex + 1).toLowerCase();
        }

        String contentType = file.getContentType();
        boolean isImageMime = contentType != null && contentType.toLowerCase().startsWith("image/");
        boolean isAllowedExt = ALLOWED_EXTENSIONS.contains(extension);

        if (!isImageMime && !isAllowedExt) {
            throw new BadRequestException("Chỉ chấp nhận các định dạng file ảnh (PNG, JPG, JPEG, WEBP, GIF).");
        }

        try {
            if (!Files.exists(UPLOAD_DIR)) {
                Files.createDirectories(UPLOAD_DIR);
            }

            String sanitizedBaseName = originalFilename
                    .replaceAll("[^a-zA-Z0-9.-]", "_")
                    .replaceAll("_{2,}", "_");
            if (sanitizedBaseName.length() > 50) {
                sanitizedBaseName = sanitizedBaseName.substring(sanitizedBaseName.length() - 50);
            }

            String uniqueFilename = UUID.randomUUID().toString() + "_" + sanitizedBaseName;
            Path destination = UPLOAD_DIR.resolve(uniqueFilename).normalize().toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/products/")
                    .path(uniqueFilename)
                    .toUriString();

            log.info("Tải lên ảnh sản phẩm thành công: file={}, url={}, size={}", uniqueFilename, fileUrl, file.getSize());

            return ResponseEntity.ok(Map.of(
                    "url", fileUrl,
                    "filename", uniqueFilename,
                    "size", file.getSize(),
                    "contentType", contentType != null ? contentType : "image/" + extension
            ));
        } catch (IOException e) {
            log.error("Lỗi khi lưu trữ file ảnh tải lên: {}", e.getMessage(), e);
            throw new BadRequestException("Không thể lưu trữ file ảnh. Vui lòng thử lại sau.");
        }
    }
}

