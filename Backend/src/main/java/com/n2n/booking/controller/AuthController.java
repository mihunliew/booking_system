package com.n2n.booking.controller;

import com.n2n.booking.dto.AuthDTOs;
import com.n2n.booking.security.UserPrincipal;
import com.n2n.booking.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthDTOs.JwtResponse> login(@Valid @RequestBody AuthDTOs.LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthDTOs.UserResponse> signup(@Valid @RequestBody AuthDTOs.SignupRequest signupRequest) {
        return ResponseEntity.ok(authService.signup(signupRequest));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthDTOs.UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(authService.getCurrentUser(userPrincipal.getId()));
    }
}
