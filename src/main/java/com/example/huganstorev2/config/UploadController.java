package com.example.huganstorev2.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private final Cloudinary cloudinary;

    public UploadController(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File không được để trống."));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Chỉ chấp nhận file hình ảnh."));
        }

        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "hugan_uploads",
                        "resource_type", "image"
                )
        );

        String url = (String) result.get("secure_url");
        return ResponseEntity.ok(Map.of("url", url));
    }
}
