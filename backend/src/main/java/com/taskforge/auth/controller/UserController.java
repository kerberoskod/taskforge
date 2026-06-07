package com.taskforge.auth.controller;

import com.taskforge.auth.dto.UserResponse;
import com.taskforge.auth.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(
            userRepository.findAll().stream().map(UserResponse::new).toList()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal(expression = "id") UUID userId) {
        return ResponseEntity.ok(
            new UserResponse(userRepository.findById(userId).orElseThrow())
        );
    }
}
