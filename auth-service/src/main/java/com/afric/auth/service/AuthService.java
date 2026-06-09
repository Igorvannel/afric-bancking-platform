package com.afric.auth.service;

import com.afric.auth.document.User;
import com.afric.auth.dto.request.LoginRequest;
import com.afric.auth.dto.request.RegisterRequest;
import com.afric.auth.dto.response.AuthResponse;
import com.afric.auth.dto.response.UserResponse;
import com.afric.auth.exception.EmailAlreadyExistsException;
import com.afric.auth.exception.ResourceNotFoundException;
import com.afric.auth.repository.UserRepository;
import com.afric.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service métier pour l'authentification et la gestion des utilisateurs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Inscription d'un nouvel utilisateur.
     * Crée l'utilisateur, génère les tokens JWT et retourne la réponse d'auth.
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("Tentative d'inscription pour : {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        User savedUser = userRepository.save(user);
        log.info("Utilisateur créé avec succès : {}", savedUser.getId());

        return buildAuthResponse(savedUser);
    }

    /**
     * Connexion d'un utilisateur existant.
     * Délègue la vérification des credentials à Spring Security.
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Tentative de connexion pour : {}", request.email());

        // Spring Security vérifie email + password via DaoAuthenticationProvider
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // Le username dans UserDetails = userId (voir CustomUserDetailsService)
        String userId = authentication.getName();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + userId));

        log.info("Connexion réussie pour : {}", user.getEmail());
        return buildAuthResponse(user);
    }

    /**
     * Récupération du profil de l'utilisateur connecté.
     */
    public UserResponse getCurrentUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + userId));
        return UserResponse.from(user);
    }

    // ─── PRIVÉ ──────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        return AuthResponse.of(accessToken, refreshToken, jwtExpiration / 1000, UserResponse.from(user));
    }
}
