package com.cinema.system.snack.service;

import com.cinema.system.common.exception.BusinessException;
import com.cinema.system.snack.dto.SnackCreateRequest;
import com.cinema.system.snack.entity.Snack;
import com.cinema.system.snack.repository.SnackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SnackService {
    private final SnackRepository snackRepository;

    public List<Snack> listSnacks() {
        return snackRepository.findByStatus("ON");
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
                .orElseThrow(() -> new BusinessException("零食不存在"));
        if (request.getName() != null) snack.setName(request.getName());
        if (request.getPrice() != null) snack.setPrice(request.getPrice());
        if (request.getImageUrl() != null) snack.setImageUrl(request.getImageUrl());
        if (request.getStock() != null) snack.setStock(request.getStock());
        return snackRepository.save(snack);
    }
}
