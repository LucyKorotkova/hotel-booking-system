package com.example.booking.controller;

import com.example.booking.entity.User;
import com.example.booking.repository.UserRepository;
import com.example.common.security.JwtProvider;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody AuthRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new IllegalStateException("Пользователь уже существует");
        }
        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .role("ROLE_USER")
                .build();
        userRepository.save(user);
        String token = jwtProvider.generateToken(user.getUsername(), user.getRole());
        return Map.of("token", token);
    }

    @PostMapping("/auth")
    public Map<String, String> login(@RequestBody AuthRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Неверный логин или пароль"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Неверный логин или пароль");
        }
        String token = jwtProvider.generateToken(user.getUsername(), user.getRole());
        return Map.of("token", token);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User createUserByAdmin(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@RequestParam Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class AuthRequest {
        private String username;
        private String password;
    }
}