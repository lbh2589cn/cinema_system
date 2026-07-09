package com.cinema.system.user.service;

import com.cinema.system.common.exception.BusinessException;
import com.cinema.system.user.dto.ChangePasswordRequest;
import com.cinema.system.user.dto.UserProfileResponse;
import com.cinema.system.user.entity.User;
import com.cinema.system.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResponse getProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));
        return toProfileResponse(user);
    }

    public void updateProfile(Long id, UserProfileResponse request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getIsMember() != null) user.setIsMember(request.getIsMember());
        userRepository.save(user);
    }

    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void deleteAccount(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));
        user.setStatus("DISABLED");
        userRepository.save(user);
    }

    private UserProfileResponse toProfileResponse(User user) {
        UserProfileResponse resp = new UserProfileResponse();
        resp.setId(user.getId());
        resp.setUserId(user.getUserId());
        resp.setUsername(user.getUsername());
        resp.setPhone(user.getPhone());
        resp.setRole(user.getRole());
        resp.setIsMember(user.getIsMember());
        resp.setStatus(user.getStatus());
        return resp;
    }
}
