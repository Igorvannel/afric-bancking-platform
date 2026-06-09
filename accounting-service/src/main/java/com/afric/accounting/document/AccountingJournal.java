package com.afric.accounting.document;

import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

/**
 * Document MongoDB représentant une ligne du journal comptable.
 * Chaque mouvement de compte (crédit/débit) génère une entrée immuable.
 * Collection : accounting_journal
 */
@Document(collection = "accounting_journal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountingJournal {

    @Id
    private String id;

    @Indexed
    @Field("accountId")
    private String accountId;

    @Field("direction")
    private Direction direction;

    @Field("amount")
    private Double amount;

    @Field("balanceBefore")
    private Double balanceBefore;

    @Field("balanceAfter")
    private Double balanceAfter;

    @CreatedDate
    @Field("createdAt")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updatedAt")
    private Instant updatedAt;

    @CreatedBy
    @Field("createdBy")
    private String createdBy;

    @LastModifiedBy
    @Field("updatedBy")
    private String updatedBy;

    /**
     * Sens du mouvement comptable.
     */
    public enum Direction {
        CREDIT, DEBIT
    }
}
