package com.afric.accounting.controller;

import com.afric.accounting.dto.request.TransactionRequest;
import com.afric.accounting.dto.response.AccountResponse;
import com.afric.accounting.dto.response.TransactionResponse;
import com.afric.accounting.service.AccountingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST Accounting.
 *
 * Routes sécurisées (Bearer token obligatoire) :
 *   GET  /api/account          → solde du compte
 *   POST /api/account/credit   → créditer
 *   POST /api/account/debit    → débiter
 *
 * L'identité utilisateur est injectée par le Gateway via X-User-Id header.
 */
@Slf4j
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountingController {

    private final AccountingService accountingService;

    /**
     * GET /api/account
     * Récupère ou initialise le compte de l'utilisateur connecté.
     */
    @GetMapping
    public ResponseEntity<AccountResponse> getAccount(HttpServletRequest request) {
        String userId = extractUserId(request);
        log.debug("GET /api/account - userId={}", userId);
        return ResponseEntity.ok(accountingService.getOrCreateAccount(userId));
    }

    /**
     * POST /api/account/credit
     * Crédite le compte de l'utilisateur connecté.
     * Header requis : Authorization: Bearer <token>
     */
    @PostMapping("/credit")
    public ResponseEntity<TransactionResponse> credit(
            @Valid @RequestBody TransactionRequest transactionRequest,
            HttpServletRequest request
    ) {
        String userId = extractUserId(request);
        log.debug("POST /api/account/credit - userId={} | amount={}", userId, transactionRequest.amount());
        TransactionResponse response = accountingService.credit(userId, transactionRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/account/debit
     * Débite le compte de l'utilisateur connecté.
     * Header requis : Authorization: Bearer <token>
     */
    @PostMapping("/debit")
    public ResponseEntity<TransactionResponse> debit(
            @Valid @RequestBody TransactionRequest transactionRequest,
            HttpServletRequest request
    ) {
        String userId = extractUserId(request);
        log.debug("POST /api/account/debit - userId={} | amount={}", userId, transactionRequest.amount());
        TransactionResponse response = accountingService.debit(userId, transactionRequest);
        return ResponseEntity.ok(response);
    }

    // ─── PRIVÉ ──────────────────────────────────────────────────────────

    /**
     * Extrait l'ID utilisateur propagé par le Gateway via header X-User-Id.
     * Ce header est injecté par JwtAuthFilter après validation du token.
     */
    private String extractUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("Header X-User-Id manquant — requête non routée via Gateway ?");
        }
        return userId;
    }
}
