package com.DataPro.controller;
import com.DataPro.entity.UserFile;
import com.DataPro.dto.UserDTO;
import com.DataPro.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
 
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
 
    private final UserService userService;
 
    public UserController(UserService userService) {
        this.userService = userService;
    }
 
    // ─────────────────────────────────────────────────────────────────
    // POST /api/users/register
    // Register a new user — welcome email sent async
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<UserDTO.ApiResponse> register(@RequestBody UserDTO.RegisterRequest request) {
        log.info("[CONTROLLER] POST /register email={}", request.getEmail());
        UserDTO.ApiResponse response = userService.register(request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
 
    // ─────────────────────────────────────────────────────────────────
    // POST /api/users/login
    // Login with email + plain password
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<UserDTO.ApiResponse> login(@RequestBody UserDTO.LoginRequest request) {
        log.info("[CONTROLLER] POST /login email={}", request.getEmail());
        UserDTO.ApiResponse response = userService.login(request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(401).body(response);
    }
 
    // ─────────────────────────────────────────────────────────────────
    // GET /api/users/find?email=abc@example.com
    // Find a single user by email
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/find")
    public ResponseEntity<UserDTO.ApiResponse> findByEmail(@RequestParam String email) {
        log.info("[CONTROLLER] GET /find email={}", email);
        UserDTO.ApiResponse response = userService.findByEmail(email);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(404).body(response);
    }
 
    // ─────────────────────────────────────────────────────────────────
    // GET /api/users/all
    // Fetch ALL users in the system (admin use)
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/all")
    public ResponseEntity<UserDTO.ApiResponse> getAllUsers() {
        log.info("[CONTROLLER] GET /all users");
        UserDTO.ApiResponse response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }
 
    // ─────────────────────────────────────────────────────────────────
    // GET /api/users/{id}
    // Fetch a single user by database ID
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO.ApiResponse> getUserById(@PathVariable Long id) {
        log.info("[CONTROLLER] GET /users/{}", id);
        UserDTO.ApiResponse response = userService.getUserById(id);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(404).body(response);
    }
 
    // ─────────────────────────────────────────────────────────────────
    // PUT /api/users/{id}
    // Update user profile — email and password not changeable here
    // ─────────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO.ApiResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UserDTO.UpdateRequest request) {
        log.info("[CONTROLLER] PUT /users/{}", id);
        UserDTO.ApiResponse response = userService.updateUser(id, request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
 
    // ─────────────────────────────────────────────────────────────────
    // DELETE /api/users/{id}
    // Delete a user by ID
    // ─────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<UserDTO.ApiResponse> deleteUser(@PathVariable Long id) {
        log.info("[CONTROLLER] DELETE /users/{}", id);
        UserDTO.ApiResponse response = userService.deleteUser(id);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(404).body(response);
    }
 
    // ─────────────────────────────────────────────────────────────────
    // POST /api/users/forgot-password
    // Send OTP to email for password reset
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<UserDTO.ApiResponse> forgotPassword(
            @RequestBody UserDTO.ForgotPasswordRequest request) {
        log.info("[CONTROLLER] POST /forgot-password email={}", request.getEmail());
        UserDTO.ApiResponse response = userService.forgotPassword(request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
 
    // ─────────────────────────────────────────────────────────────────
    // POST /api/users/reset-password
    // Verify OTP and set new password
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<UserDTO.ApiResponse> resetPassword(
            @RequestBody UserDTO.ResetPasswordRequest request) {
        log.info("[CONTROLLER] POST /reset-password email={}", request.getEmail());
        UserDTO.ApiResponse response = userService.resetPassword(request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
}
 