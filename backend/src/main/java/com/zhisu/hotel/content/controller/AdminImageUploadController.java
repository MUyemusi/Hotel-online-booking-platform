package com.zhisu.hotel.content.controller;

import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.systemconfig.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/uploads")
@RequiredArgsConstructor
public class AdminImageUploadController {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private final SystemConfigService systemConfigService;

    @Value("${hotel.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/images")
    public ApiResponse<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new BusinessException("IMAGE_FILE_INVALID", "请选择有效的图片文件");
        }
        int maxMb = systemConfigService.getInteger("IMAGE_UPLOAD_MAX_MB");
        if (file.getSize() > maxMb * 1024L * 1024L) {
            throw new BusinessException("IMAGE_FILE_TOO_LARGE", "图片不能超过 " + maxMb + " MB");
        }
        String original = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String extension = original.contains(".") ? original.substring(original.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("IMAGE_EXTENSION_INVALID", "仅支持 jpg、png、gif、webp 图片");
        }
        verifyImageSignature(file, extension);
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path directory = Path.of(uploadDir).toAbsolutePath().normalize();
        Path target = directory.resolve(filename).normalize();
        if (!target.startsWith(directory)) throw new BusinessException("IMAGE_PATH_INVALID", "图片路径无效");
        try {
            Files.createDirectories(directory);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new BusinessException("IMAGE_SAVE_FAILED", "图片保存失败");
        }
        return ApiResponse.success(Map.of("url", "/uploads/" + filename));
    }

    private void verifyImageSignature(MultipartFile file, String extension) {
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(12);
            boolean jpg = header.length >= 3 && (header[0] & 0xff) == 0xff && (header[1] & 0xff) == 0xd8 && (header[2] & 0xff) == 0xff;
            boolean png = header.length >= 8 && (header[0] & 0xff) == 0x89 && header[1] == 0x50 && header[2] == 0x4e && header[3] == 0x47;
            boolean gif = header.length >= 6 && new String(header, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("GIF8");
            boolean webp = header.length >= 12
                    && new String(header, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF")
                    && new String(header, 8, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP");
            boolean valid = switch (extension) {
                case "jpg", "jpeg" -> jpg;
                case "png" -> png;
                case "gif" -> gif;
                case "webp" -> webp;
                default -> false;
            };
            if (!valid) throw new BusinessException("IMAGE_CONTENT_INVALID", "文件内容与图片格式不匹配");
        } catch (IOException e) {
            throw new BusinessException("IMAGE_READ_FAILED", "无法读取图片文件");
        }
    }
}