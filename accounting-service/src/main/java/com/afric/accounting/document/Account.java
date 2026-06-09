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
 * Document MongoDB représentant un compte bancaire.
 * Chaque utilisateur possède un compte unique.
 * Collection : account
 */
@Document(collection = "account")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("userId")
    private String userId;

    @Indexed(unique = true)
    @Field("accountNumber")
    private String accountNumber;

    @Field("balance")
    private Double balance;

    @Field("currency")
    private String currency;

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
}
