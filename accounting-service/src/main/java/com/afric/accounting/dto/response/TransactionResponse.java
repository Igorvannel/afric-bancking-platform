package com.afric.accounting.dto.response;

import com.afric.accounting.document.AccountingJournal;

import java.time.Instant;

/**
 * DTO retourné après une opération de crédit ou débit.
 * Inclut le solde mis à jour et l'entrée journal créée.
 */
public record TransactionResponse(
    String journalId,
    String accountId,
    String direction,
    Double amount,
    Double balanceBefore,
    Double balanceAfter,
    Instant createdAt
) {
    public static TransactionResponse from(AccountingJournal journal) {
        return new TransactionResponse(
            journal.getId(),
            journal.getAccountId(),
            journal.getDirection().name(),
            journal.getAmount(),
            journal.getBalanceBefore(),
            journal.getBalanceAfter(),
            journal.getCreatedAt()
        );
    }
}
