package com.afric.accounting.service;

import com.afric.accounting.document.Account;
import com.afric.accounting.document.AccountingJournal;
import com.afric.accounting.document.AccountingJournal.Direction;
import com.afric.accounting.dto.request.TransactionRequest;
import com.afric.accounting.dto.response.AccountResponse;
import com.afric.accounting.dto.response.TransactionResponse;
import com.afric.accounting.exception.AccountNotFoundException;
import com.afric.accounting.exception.InsufficientFundsException;
import com.afric.accounting.repository.AccountRepository;
import com.afric.accounting.repository.AccountingJournalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service métier pour les opérations comptables.
 *
 * IMPORTANT : Les méthodes credit() et debit() sont @Transactional.
 * La cohérence est garantie par le Replica Set MongoDB qui permet
 * les transactions multi-documents ACID.
 *
 * Principe du journal comptable :
 *   1. Lire le solde actuel (balanceBefore)
 *   2. Calculer le nouveau solde (balanceAfter)
 *   3. Mettre à jour le compte
 *   4. Écrire une ligne dans accounting_journal
 *   → Si l'une des étapes échoue : rollback total (MongoDB txn)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountingService {

    private final AccountRepository accountRepository;
    private final AccountingJournalRepository journalRepository;

    /**
     * Récupère ou crée le compte d'un utilisateur.
     * Appelé automatiquement lors de la première opération.
     */
    public AccountResponse getOrCreateAccount(String userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseGet(() -> createAccount(userId));
        return AccountResponse.from(account);
    }

    /**
     * POST /api/account/credit
     * Crédite le compte d'un utilisateur.
     * Écrit atomiquement dans account ET accounting_journal.
     */
    @Transactional
    public TransactionResponse credit(String userId, TransactionRequest request) {
        log.info("CREDIT | userId={} | amount={}", userId, request.amount());

        Account account = getAccountOrThrow(userId);
        double balanceBefore = account.getBalance();
        double balanceAfter = balanceBefore + request.amount();

        // Mise à jour du solde
        account.setBalance(balanceAfter);
        accountRepository.save(account);

        // Écriture de la ligne journal
        AccountingJournal journal = AccountingJournal.builder()
                .accountId(account.getId())
                .direction(Direction.CREDIT)
                .amount(request.amount())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .build();

        AccountingJournal savedJournal = journalRepository.save(journal);
        log.info("CREDIT OK | accountId={} | {} -> {}", account.getId(), balanceBefore, balanceAfter);

        return TransactionResponse.from(savedJournal);
    }

    /**
     * POST /api/account/debit
     * Débite le compte d'un utilisateur.
     * Vérifie la provision avant toute opération.
     * Écrit atomiquement dans account ET accounting_journal.
     */
    @Transactional
    public TransactionResponse debit(String userId, TransactionRequest request) {
        log.info("DEBIT | userId={} | amount={}", userId, request.amount());

        Account account = getAccountOrThrow(userId);
        double balanceBefore = account.getBalance();

        // Vérification de la provision
        if (balanceBefore < request.amount()) {
            throw new InsufficientFundsException(balanceBefore, request.amount());
        }

        double balanceAfter = balanceBefore - request.amount();

        // Mise à jour du solde
        account.setBalance(balanceAfter);
        accountRepository.save(account);

        // Écriture de la ligne journal
        AccountingJournal journal = AccountingJournal.builder()
                .accountId(account.getId())
                .direction(Direction.DEBIT)
                .amount(request.amount())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .build();

        AccountingJournal savedJournal = journalRepository.save(journal);
        log.info("DEBIT OK | accountId={} | {} -> {}", account.getId(), balanceBefore, balanceAfter);

        return TransactionResponse.from(savedJournal);
    }

    // ─── PRIVÉ ──────────────────────────────────────────────────────────

    private Account getAccountOrThrow(String userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException(userId));
    }

    private Account createAccount(String userId) {
        Account account = Account.builder()
                .userId(userId)
                .accountNumber(generateAccountNumber())
                .balance(0.0)
                .currency("XAF")
                .build();
        Account saved = accountRepository.save(account);
        log.info("Nouveau compte créé : {} pour userId={}", saved.getAccountNumber(), userId);
        return saved;
    }

    private String generateAccountNumber() {
        // Format : AF + 12 chiffres aléatoires
        return "AF" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12).toUpperCase();
    }
}
