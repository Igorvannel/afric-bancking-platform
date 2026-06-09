package com.afric.accounting.repository;

import com.afric.accounting.document.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {

    Optional<Account> findByUserId(String userId);

    boolean existsByUserId(String userId);

    Optional<Account> findByAccountNumber(String accountNumber);
}
