package org.clarkproject.aioapi.api.service;

import lombok.extern.slf4j.Slf4j;
import org.clarkproject.aioapi.api.exception.IllegalObjectStatusException;
import org.clarkproject.aioapi.api.obj.dto.Member;
import org.clarkproject.aioapi.api.configure.MemberConfig;
import org.clarkproject.aioapi.api.obj.po.MemberPO;
import org.clarkproject.aioapi.api.obj.enums.MemberRole;
import org.clarkproject.aioapi.api.obj.enums.MemberStatus;
import org.clarkproject.aioapi.api.repository.MemberRepository;
import org.clarkproject.aioapi.api.exception.ValidationException;
import org.clarkproject.aioapi.api.tool.MemberMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * 找出正在使用的帳號
     *
     * @param account 帳號搜尋
     * @return MemberPO Optional
     */
    public Optional<MemberPO> findActiveAccount(String account) {
        return Optional.ofNullable(memberRepository.findByAccount(account))
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
    public void updateMemberStatus(MemberPO memberPO,boolean isPasswordMeet) {
        if (isPasswordMeet) {
            memberPO.setLastLogin(LocalDateTime.now());
            memberPO.setLoginAttempts(0);
        } else {
            memberPO.setLoginAttempts(memberPO.getLoginAttempts() + 1);
            boolean isLoginAttemptsOver = memberPO.getLoginAttempts() >= MemberConfig.ACCOUNT_RETRY_LIMIT;
            if (isLoginAttemptsOver) {
                log.info("Login error limitation exceeded! please try after 30 mins");
                //TODO 三十分鐘鎖定
            }
        }
        memberRepository.save(memberPO);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean loginWithBasicToken(Member member, String accessIp) throws ValidationException {
        MemberPO memberPO = findActiveAccount(member.getAccount())
                .orElseThrow(() -> new ValidationException("account not available"));

        boolean isLoginIPMeet = memberPO.getIp().getHostAddress().equals(accessIp);
        if (!isLoginIPMeet) {
            log.info("Login IP Change! former ip is {},login ip is {}", memberPO.getIp(), accessIp);
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
                //TODO 三十分鐘鎖定
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
    public boolean update(Member member, String accessIp) throws IllegalObjectStatusException {
        MemberPO memberPO = findActiveAccount(member.getAccount())
                .orElseThrow(() -> new IllegalObjectStatusException("account not available"));

        boolean isIPMeet = memberPO.getIp().getHostAddress().equals(accessIp);
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
        if (member.getName() != null && !member.getName().isEmpty()) {
            memberPO.setName(member.getName());
        }
        if (member.getPassword() != null && !member.getPassword().isEmpty()) {
            memberPO.setPassword(member.getPassword());
        }
        if (member.getEmail() != null && !member.getEmail().isEmpty()) {
            memberPO.setEmail(member.getEmail());
        }
        if (member.getPhone() != null && !member.getPhone().isEmpty()) {
            memberPO.setPhoneNumber(member.getPhone());
        }
        if (member.getAddress() != null && !member.getAddress().isEmpty()) {
            memberPO.setAddress(member.getAddress());
        }
        if (member.getBirthday() != null) {
            memberPO.setBirthdate(member.getBirthday());
        }
        saveMember(memberPO);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean disableMember(Long id, String accessIp) throws IllegalObjectStatusException {
        MemberPO memberPO = memberRepository.findById(id).filter( m -> m.getStatus().equals(MemberStatus.ACTIVE.name()))
                .orElseThrow(() -> new IllegalObjectStatusException("account not available"));

        boolean isIPMeet = memberPO.getIp().getHostAddress().equals(accessIp);
        if (isIPMeet) {
            log.info("Login IP Change! former ip is {},now ip is {}", memberPO.getIp(), accessIp);
        }
        memberPO.setStatus(MemberStatus.INACTIVE.name());

        try {
            saveMember(memberPO);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean frozeMember(Long id, String accessIp, Long adminId) throws IllegalObjectStatusException {
        boolean isAdminLevel = isAdmin(adminId);
        if (!isAdminLevel) {
            throw new IllegalObjectStatusException("account could not access authority");
        }
        MemberPO memberPO = findAccountById(id).orElseThrow(
                () -> new IllegalObjectStatusException("account not available")
        );

        boolean isIPMeet = memberPO.getIp().getHostAddress().equals(accessIp);
        if (isIPMeet) {
            log.info("Login IP Change! former ip is {},now ip is {}", memberPO.getIp(), accessIp);
        }
        memberPO.setStatus(MemberStatus.SUSPENDED.name());

        try {
            saveMember(memberPO);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isAdmin(Long adminId) {
        return memberRepository.findById(adminId)
                .filter( m -> m.getStatus().equals(MemberStatus.ACTIVE.name()))
                .filter(m -> m.getRole().equals(MemberRole.ADMIN.name()))
                .isPresent();
    }

    public List<Member> findAllActiveMember() {
        return memberRepository.findAllByStatus(MemberStatus.ACTIVE.name())
                .stream()
                .map(MemberMapper.INSTANCE::memberPOToMember)
                .collect(Collectors.toList());
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

    public static InetAddress stringToInetAddress(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Invalid IP address: " + ip, e);
        }
    }
}
