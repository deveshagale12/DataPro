package com.DataPro.controller;

import com.DataPro.dto.UserDTO;
import com.DataPro.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")

public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /api/users/register
    @PostMapping("/register")
    public ResponseEntity<UserDTO.ApiResponse> register(@RequestBody UserDTO.RegisterRequest request) {
        UserDTO.ApiResponse response = userService.register(request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    // POST /api/users/login
    @PostMapping("/login")
    public ResponseEntity<UserDTO.ApiResponse> login(@RequestBody UserDTO.LoginRequest request) {
        UserDTO.ApiResponse response = userService.login(request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(401).body(response);
    }

    // GET /api/users/find?email=abc@example.com
    @GetMapping("/find")
    public ResponseEntity<UserDTO.ApiResponse> findByEmail(@RequestParam String email) {
        UserDTO.ApiResponse response = userService.findByEmail(email);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(404).body(response);
    }

    // POST /api/users/forgot-password
    @PostMapping("/forgot-password")
    public ResponseEntity<UserDTO.ApiResponse> forgotPassword(@RequestBody UserDTO.ForgotPasswordRequest request) {
        UserDTO.ApiResponse response = userService.forgotPassword(request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    // POST /api/users/reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<UserDTO.ApiResponse> resetPassword(@RequestBody UserDTO.ResetPasswordRequest request) {
        UserDTO.ApiResponse response = userService.resetPassword(request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
}