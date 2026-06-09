package com.afric.accounting.repository;

import com.afric.accounting.document.AccountingJournal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountingJournalRepository extends MongoRepository<AccountingJournal, String> {

    Page<AccountingJournal> findByAccountIdOrderByCreatedAtDesc(String accountId, Pageable pageable);

    List<AccountingJournal> findTop10ByAccountIdOrderByCreatedAtDesc(String accountId);
}
