package org.clarkproject.aioapi.api.repository;

import org.clarkproject.aioapi.api.obj.po.MemberPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface MemberRepository extends JpaRepository<MemberPO, Long> {
    MemberPO findByAccount(String account);
    List<MemberPO> findAllByStatus(String status);
}
