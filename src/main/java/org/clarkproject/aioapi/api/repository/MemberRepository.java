package org.clarkproject.aioapi.api.repository;

import org.clarkproject.aioapi.api.orm.MemberPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<MemberPO, Long> {
    public MemberPO findByAccount(String account);
    public MemberPO findByAccountAndStatus(String account, String status);
}
