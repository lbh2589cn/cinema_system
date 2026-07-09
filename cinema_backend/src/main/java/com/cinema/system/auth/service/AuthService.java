package com.cinema.system.auth.service;

import com.cinema.system.auth.dto.LoginRequest;
import com.cinema.system.auth.dto.LoginResponse;
import com.cinema.system.auth.dto.RegisterRequest;
import com.cinema.system.auth.util.JwtUtil;
import com.cinema.system.common.exception.BusinessException;
import com.cinema.system.user.entity.User;
import com.cinema.system.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void register(RegisterRequest request) {
        if (userRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new BusinessException("Account already exists");
        }

        User user = new User();
        user.setUserId(request.getUserId());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole("USER");
        user.setIsMember(false);
        user.setStatus("ACTIVE");
        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new BusinessException("Invalid account or password"));

        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException("Account has been disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Invalid account or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getUserId(),
                user.getUsername(), user.getRole(), user.getIsMember());
    }
}
