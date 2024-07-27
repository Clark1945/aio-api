package org.clarkproject.aioapi.api.service;

import lombok.extern.slf4j.Slf4j;
import org.clarkproject.aioapi.api.obj.Member;
import org.clarkproject.aioapi.api.orm.MemberConfig;
import org.clarkproject.aioapi.api.orm.MemberPO;
import org.clarkproject.aioapi.api.orm.MemberRole;
import org.clarkproject.aioapi.api.orm.MemberStatus;
import org.clarkproject.aioapi.api.repository.MemberRepository;
import org.clarkproject.aioapi.api.tool.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service("MemberService")
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Deprecated
    public MemberPO findAccount1(Member member) {
        MemberPO memberPoCheck = memberRepository.findByAccount(member.getAccount());
        if (memberPoCheck != null) {
            if (Objects.equals(memberPoCheck.getStatus(), MemberStatus.ACTIVE.name())) {
                return memberPoCheck;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public Optional<MemberPO> findActiveAccount(Member member) {
        return Optional.ofNullable(memberRepository.findByAccount(member.getAccount()))
                .filter(m -> m.getStatus().equals(MemberStatus.ACTIVE.name()));
    }

    public Optional<MemberPO> findAccountById(Long id) {
        return memberRepository.findById(id);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class, timeout = 30)
    public void saveMember(MemberPO memberPO) {
        memberRepository.save(memberPO);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean login(Member member, String accessIp) throws ValidationException {
        MemberPO memberPO = memberRepository.findByAccount(member.getAccount());
        if (memberPO == null) {
            throw new ValidationException("account not found");
        }
        if (!memberPO.getStatus().equals(MemberStatus.ACTIVE.name())) {
            throw new ValidationException("account not active");
        }
        boolean isIPMeet = memberPO.getIp().equals(accessIp);
        if (!isIPMeet) {
            log.info("Login IP Change! former ip is {},login ip is {}", memberPO.getIp(), member.getIp());
        }
        boolean isPasswordMeet = memberPO.getPassword().equals(member.getPassword());
        if (isPasswordMeet) {
            memberPO.setLastLogin(LocalDateTime.now());
            memberPO.setLoginAttempts(0);
        } else {
            memberPO.setLoginAttempts(memberPO.getLoginAttempts() + 1);
            boolean isLoginAttemptsOver = memberPO.getLoginAttempts() >= MemberConfig.ACCOUNT_RETRY_LIMIT;
            if (isLoginAttemptsOver) {
                log.info("Login error limitation exceeded! please try after 30 mins");
                return false;
            }
        }

        try {
            memberRepository.save(memberPO);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return isPasswordMeet;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean update(Member member, String accessIp) throws ValidationException {
        MemberPO memberPO = memberRepository.findByAccount(member.getAccount());
        if (memberPO == null) {
            throw new ValidationException("account not found");
        }
        if (!memberPO.getStatus().equals(MemberStatus.ACTIVE.name())) {
            throw new ValidationException("account not active");
        }
        boolean isIPMeet = memberPO.getIp().equals(accessIp);
        if (isIPMeet) {
            log.info("Login IP Change! former ip is {},now ip is {}", memberPO.getIp(), accessIp);
        }
        try {
            updateMember(member, memberPO);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class, timeout = 30)
    void updateMember(Member member, MemberPO memberPO) {
        if (member.getName() != null) {
            memberPO.setName(member.getName());
        }
        if (member.getPassword() != null) {
            memberPO.setPassword(member.getPassword());
        }
        if (member.getEmail() != null) {
            memberPO.setEmail(member.getEmail());
        }
        if (member.getPhone() != null) {
            memberPO.setPhoneNumber(member.getPhone());
        }
        if (member.getAddress() != null) {
            memberPO.setAddress(member.getAddress());
        }
        if (member.getRole() != null) {
            memberPO.setRole(member.getRole().name());
        }
        if (member.getBirthday() != null) {
            memberPO.setBirthdate(member.getBirthday());
        }
        memberRepository.save(memberPO);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean disableMember(Long id, String accessIp) throws ValidationException {
        MemberPO memberPO = memberRepository.findById(id)
                .orElseThrow(() -> new ValidationException("account not found"));
        if (!memberPO.getStatus().equals(MemberStatus.ACTIVE.name())) {
            throw new ValidationException("account not active");
        }

        boolean isIPMeet = memberPO.getIp().equals(accessIp);
        if (isIPMeet) {
            log.info("Login IP Change! former ip is {},now ip is {}", memberPO.getIp(), accessIp);
        }
        memberPO.setStatus(MemberStatus.INACTIVE.name());

        try {
            memberRepository.save(memberPO);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean frozeMember(Long id, String accessIp, Long adminId) throws ValidationException {
        MemberPO memberPO = memberRepository.findById(id)
                .orElseThrow(() -> new ValidationException("MemberNot Exist"));
        boolean isAdmin = memberRepository.findById(adminId).filter(m -> m.getRole().equals(MemberRole.ADMIN.name())).isPresent();
        if (!isAdmin) {
            log.info("authority not allowed");
            return false;
        }
        boolean isIPMeet = memberPO.getIp().equals(accessIp);
        if (isIPMeet) {
            log.info("Login IP Change! former ip is {},now ip is {}", memberPO.getIp(), accessIp);
        }
        memberPO.setStatus(MemberStatus.SUSPENDED.name());

        try {
            memberRepository.save(memberPO);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
