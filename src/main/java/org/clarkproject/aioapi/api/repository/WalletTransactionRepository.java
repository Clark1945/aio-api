package org.clarkproject.aioapi.api.repository;

import org.clarkproject.aioapi.api.orm.WalletTransactionPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository extends JpaRepository<WalletTransactionPO,Long> {
}
