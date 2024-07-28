package org.clarkproject.aioapi.api.repository;

import org.clarkproject.aioapi.api.orm.WalletTransactionPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository extends JpaRepository<WalletTransactionPO,Long> {
    Optional<List<WalletTransactionPO>> findAllByWalletId(Long walletId);
}
