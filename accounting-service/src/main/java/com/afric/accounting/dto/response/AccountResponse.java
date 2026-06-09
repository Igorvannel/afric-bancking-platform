package com.afric.accounting.dto.response;

import com.afric.accounting.document.Account;

import java.time.Instant;

/**
 * DTO compte exposé au frontend.
 */
public record AccountResponse(
    String id,
    String userId,
    String accountNumber,
    Double balance,
    String currency,
    Instant createdAt
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getUserId(),
            account.getAccountNumber(),
            account.getBalance(),
            account.getCurrency(),
            account.getCreatedAt()
        );
    }
}
