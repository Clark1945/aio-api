package org.clarkproject.aioapi.api.repository;

import org.clarkproject.aioapi.api.orm.WalletPO;
import org.springframework.data.jpa.repository.JpaRepository;


public interface WalletRepository extends JpaRepository<WalletPO, Long> {
}
