package com.hh.mirishop.user.service;

import com.hh.mirishop.file.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class ImageUploadService {

    private final Path imageDirectory;

    public ImageUploadService() {
        this.imageDirectory = Paths.get("uploads/images")
                .toAbsolutePath()
                .normalize();
        createDirectories();
    }

    private void createDirectories() {
        try {
            Files.createDirectories(imageDirectory);
        } catch (IOException e) {
            log.error("이미지 저장 폴더 생성 실패", e);
            throw new RuntimeException("이미지 저장 폴더 생성 실패", e);
        }
    }

    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalStateException("업로드할 이미지가 없습니다.");
        }

        if (!isValidImageType(file)) {
            throw new IllegalStateException("지원하지 않는 이미지 형식입니다.");
        }

        String filename = FileUtils.generateUniqueFileName(file.getOriginalFilename());
        Path destinationFilePath = imageDirectory.resolve(filename);
        Files.copy(file.getInputStream(), destinationFilePath);

        return filename; // 저장된 이미지 파일명 반환
    }

    // 파일명은 jpg, png, gif만 허용
    private boolean isValidImageType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.contains("image/jpeg") ||
                contentType.contains("image/png") ||
                contentType.contains("image/gif"));
    }
}