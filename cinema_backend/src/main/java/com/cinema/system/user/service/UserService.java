package com.cinema.system.user.service;

import com.cinema.system.common.exception.BusinessException;
import com.cinema.system.user.dto.UserProfileResponse;
import com.cinema.system.user.entity.User;
import com.cinema.system.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserProfileResponse getProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return toProfileResponse(user);
    }

    public void updateProfile(Long id, UserProfileResponse request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getNickname() != null) user.setNickname(request.getNickname());
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        userRepository.save(user);
    }

    private UserProfileResponse toProfileResponse(User user) {
        UserProfileResponse resp = new UserProfileResponse();
        resp.setId(user.getId());
        resp.setUserId(user.getUserId());
        resp.setUsername(user.getUsername());
        resp.setPhone(user.getPhone());
        resp.setNickname(user.getNickname());
        resp.setRole(user.getRole());
        resp.setIsMember(user.getIsMember());
        resp.setStatus(user.getStatus());
        return resp;
    }
}
