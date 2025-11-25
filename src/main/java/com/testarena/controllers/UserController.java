package com.testarena.controllers;

import com.testarena.dto.*;
import com.testarena.services.UserService;
import com.testarena.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> signup(@RequestBody SignupRequest request) {
        userService.signup(request);
        return ResponseEntity.status(201).body(new MessageResponse("Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        String token = userService.login(request);
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> profile(@RequestHeader("Authorization") String header) {
        String email = jwtUtil.extractEmail(header.substring(7));
        return ResponseEntity.ok(userService.getProfile(email));
    }

    @PutMapping("/profile")
    public ResponseEntity<MessageResponse> updateProfile(
            @RequestHeader("Authorization") String header,
            @RequestBody UpdateProfileRequest request) {

        String email = jwtUtil.extractEmail(header.substring(7));
        userService.updateProfile(email, request);
        return ResponseEntity.ok(new MessageResponse("Profile updated successfully"));
    }
}
