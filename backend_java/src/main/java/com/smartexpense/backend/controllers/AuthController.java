package com.smartexpense.backend.controllers;

import com.smartexpense.backend.models.User;
import com.smartexpense.backend.services.UserService;
import com.smartexpense.backend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            String name = request.get("name");

            if (email == null || password == null || name == null) {
                return ResponseEntity.badRequest().body("Email, password, and name are required");
            }

            User user = new User();
            user.setEmail(email);
            user.setPassword(password);
            user.setName(name);

            User savedUser = userService.registerUser(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", savedUser.getId());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest().body("Email and password are required");
            }

            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (userService.validatePassword(user, password)) {

                    String token = jwtUtils.generateJwtToken(user.getId());
                    Map<String, Object> response = new HashMap<>();
                    response.put("token", token);
                    response.put("user", Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "currency", user.getCurrency()
                    ));
                    return ResponseEntity.ok(response);
                }
            }
            return ResponseEntity.status(401).body("Invalid credentials");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // In a stateless JWT system, logout is handled client-side by removing the token
        return ResponseEntity.ok("Logged out successfully");
    }
}
