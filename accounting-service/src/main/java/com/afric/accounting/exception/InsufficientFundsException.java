package com.afric.accounting.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(Double balance, Double amount) {
        super(String.format("Solde insuffisant. Solde disponible : %.2f XAF, montant demandé : %.2f XAF", balance, amount));
    }
}
