package com.cinema.system.snack.service;

import com.cinema.system.common.exception.BusinessException;
import com.cinema.system.snack.dto.SnackCreateRequest;
import com.cinema.system.snack.entity.Snack;
import com.cinema.system.snack.repository.SnackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SnackService {
    private final SnackRepository snackRepository;

    private final Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "images");

    public List<Snack> listSnacks() {
        return snackRepository.findByStatus("ON");
    }

    public List<Snack> listAllSnacks() {
        return snackRepository.findAll();
    }

    public Snack createSnack(SnackCreateRequest request) {
        Snack snack = new Snack();
        snack.setName(request.getName());
        snack.setPrice(request.getPrice());
        snack.setImageUrl(request.getImageUrl());
        snack.setStock(request.getStock());
        snack.setStatus("ON");
        return snackRepository.save(snack);
    }

    public Snack updateSnack(Long id, SnackCreateRequest request) {
        Snack snack = snackRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Snack not found"));
        if (request.getImageUrl() != null && !request.getImageUrl().equals(snack.getImageUrl())) {
            deleteImageFile(snack.getImageUrl());
        }
        if (request.getName() != null) snack.setName(request.getName());
        if (request.getPrice() != null) snack.setPrice(request.getPrice());
        if (request.getImageUrl() != null) snack.setImageUrl(request.getImageUrl());
        if (request.getStock() != null) snack.setStock(request.getStock());
        if (request.getStatus() != null) snack.setStatus(request.getStatus());
        return snackRepository.save(snack);
    }

    public void deleteSnack(Long id) {
        Snack snack = snackRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Snack not found"));
        deleteImageFile(snack.getImageUrl());
        snackRepository.delete(snack);
    }

    private void deleteImageFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;
        try {
            String relativePath = imageUrl.replace("/uploads/images/", "");
            Path filePath = uploadDir.resolve(relativePath);
            Files.deleteIfExists(filePath);
            Path parentDir = filePath.getParent();
            if (parentDir != null && !parentDir.equals(uploadDir)) {
                try { Files.deleteIfExists(parentDir); } catch (IOException ignored) {}
            }
        } catch (IOException ignored) {
        }
    }
}
