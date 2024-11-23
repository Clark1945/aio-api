package org.clarkproject.aioapi.api.repository;

import org.clarkproject.aioapi.api.obj.po.MemberPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface MemberRepository extends JpaRepository<MemberPO, Long> {
    MemberPO findByAccount(String account);
    MemberPO findByEmail(String email);
    List<MemberPO> findAllByStatus(String status);
    @Query("SELECT p FROM MemberPO p WHERE p.account = :account AND p.status = :status")
    MemberPO findByNameAndStatus(String account, String status);
}
