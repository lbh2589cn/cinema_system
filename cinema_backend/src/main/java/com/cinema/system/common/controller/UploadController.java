package com.cinema.system.common.controller;

import com.cinema.system.common.response.ApiResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "images");

    @PostMapping
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file,
                                      @RequestParam(defaultValue = "") String directory) {
        if (file.isEmpty()) {
            return ApiResponse.error(400, "Please select a file");
        }

        try {
            Path targetDir = uploadDir;
            if (!directory.isEmpty()) {
                targetDir = uploadDir.resolve(directory);
            }
            Files.createDirectories(targetDir);

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;

            Path targetPath = targetDir.resolve(filename);
            file.transferTo(targetPath.toFile());

            String url = "/uploads/images/" + filename;
            if (!directory.isEmpty()) {
                url = "/uploads/images/" + directory + "/" + filename;
            }
            return ApiResponse.success(url);
        } catch (IOException e) {
            return ApiResponse.error(500, "File upload failed: " + e.getMessage());
        }
    }
}
