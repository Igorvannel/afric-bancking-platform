package com.afric.auth.controller;

import com.afric.auth.dto.request.LoginRequest;
import com.afric.auth.dto.request.RegisterRequest;
import com.afric.auth.dto.response.AuthResponse;
import com.afric.auth.dto.response.UserResponse;
import com.afric.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST Auth.
 *
 * Routes publiques  : POST /api/register, POST /api/login
 * Routes sécurisées : GET  /api/user
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/register
     * Créer un nouveau compte utilisateur.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.debug("POST /api/register - email={}", request.email());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/login
     * Authentifier un utilisateur et obtenir un token JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("POST /api/login - email={}", request.email());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/user
     * Récupérer le profil de l'utilisateur authentifié.
     * Header requis : Authorization: Bearer <token>
     */
    @GetMapping("/user")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("GET /api/user - userId={}", userDetails.getUsername());
        UserResponse response = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
