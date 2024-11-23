package org.clarkproject.aioapi.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clarkproject.aioapi.api.exception.IllegalObjectStatusException;
import org.clarkproject.aioapi.api.obj.MemberUserDetails;
import org.clarkproject.aioapi.api.obj.dto.Member;
import org.clarkproject.aioapi.api.configure.MemberConfig;
import org.clarkproject.aioapi.api.obj.po.MemberPO;
import org.clarkproject.aioapi.api.obj.enums.MemberRole;
import org.clarkproject.aioapi.api.obj.enums.MemberStatus;
import org.clarkproject.aioapi.api.repository.MemberRepository;
import org.clarkproject.aioapi.api.exception.ValidationException;
import org.clarkproject.aioapi.api.tool.MemberMapper;
import org.clarkproject.aioapi.api.tool.UserIdIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;


@Slf4j
@Service
public class MemberService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserIdIdentity userIdentity;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    @Autowired
    public MemberService(MemberRepository memberRepository,
                         PasswordEncoder passwordEncoder,
                         UserIdIdentity userIdentity,
                         AuthenticationManager authenticationManager,
                         JWTService jwtService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.userIdentity = userIdentity;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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


    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void saveMember(MemberPO memberPO) {
        memberRepository.saveAndFlush(memberPO);
    }

    /**
     * 根據登入密碼檢核判斷是否要鎖定帳戶
     *
     * @param memberPO
     * @param isPasswordMeet
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateMemberStatus(MemberPO memberPO, boolean isPasswordMeet) {
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
        memberRepository.saveAndFlush(memberPO);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Deprecated
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


    public void update(MemberPO memberPO, Member newMember, String accessIp) throws IllegalObjectStatusException {
        boolean isIPMeet = memberPO.getIp().getHostAddress().equals(accessIp);
        if (isIPMeet) {
            log.warn("Login IP Change! former ip is {},now ip is {}", memberPO.getIp(), accessIp);
        }
        try {
            updateMember(newMember, memberPO);
        } catch (Exception e) {
            System.out.println("hehehe");
        }

    }

    @Autowired
    @Lazy // 實現Transaction自調用，解決不Rollback問題，避免循環引入而出錯 但拆開才是Best Practice
    private MemberService memberService;

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
        memberService.saveMember(memberPO);
    }

    public boolean disableMember(MemberPO memberPO, String accessIp) throws IllegalObjectStatusException {
        boolean isIPMeet = memberPO.getIp().getHostAddress().equals(accessIp);
        if (isIPMeet) {
            log.info("Login IP Change! former ip is {},now ip is {}", memberPO.getIp(), accessIp);
        }
        memberPO.setStatus(MemberStatus.INACTIVE.name());

        try {
            memberService.saveMember(memberPO);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean frozeMember(String account) throws IllegalObjectStatusException {

        MemberPO memberPO = Optional.ofNullable(memberRepository.findByNameAndStatus(account, MemberStatus.ACTIVE.name()))
                .orElseThrow(() -> new IllegalObjectStatusException("account not available")
                );
        memberPO.setStatus(MemberStatus.SUSPENDED.name());

        try {
            memberService.saveMember(memberPO);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Deprecated
    private boolean isAdmin(Long adminId) {
        return memberRepository.findById(adminId)
                .filter(m -> m.getStatus().equals(MemberStatus.ACTIVE.name()))
                .filter(m -> m.getRole().equals(MemberRole.ADMIN.name()))
                .isPresent();
    }

    public static InetAddress stringToInetAddress(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Invalid IP address: " + ip, e);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void register(Member member, String accessIp) {
        boolean isAccountUsed = findActiveAccount(member.getAccount()).isPresent();
        boolean isEmailUsed = memberRepository.findByEmail(member.getEmail()) != null;
        if (isAccountUsed || isEmailUsed) {
            throw new IllegalObjectStatusException("Member already exists");
        }
        MemberPO memberPO = MemberMapper.memberToMemberPo(member, accessIp);
        saveMember(memberPO);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, timeout = 30)
    public boolean login(MemberPO memberPO, String accessIp, String password) {
        boolean isLoginIPMeet = memberPO.getIp().getHostAddress().equals(accessIp);
        if (!isLoginIPMeet) {
            log.warn("Login IP Change! former ip is {},login ip is {}", memberPO.getIp(), accessIp);
        }

        boolean isPasswordMeet = passwordEncoder.matches(password, memberPO.getPassword());
        updateMemberStatus(memberPO, isPasswordMeet);
        return isPasswordMeet;
    }

    /**
     * 驗證BASIC Token 如果成功回傳username
     *
     * @return
     */
    public String validateWithBasicToken() {
        if (userIdentity.isAnonymous()) {
            throw new ValidationException("你尚未經過身份認證");
        }

        System.out.printf(
                "你的帳號：%s%n信箱：%s%n權限：%s%n",
                userIdentity.getUsername(),
                userIdentity.getEmail(),
                userIdentity.getAuthority()
        );

        return userIdentity.getUsername();
    }

    public MemberUserDetails getJWTMember(Member member) {
        Authentication token = new UsernamePasswordAuthenticationToken(
                member.getAccount(),
                member.getPassword()
        );
        Authentication auth = authenticationManager.authenticate(token);
        return (MemberUserDetails) auth.getPrincipal();
    }

    public String getJWTToken(MemberUserDetails user) {
        String jwt = jwtService.createLoginAccessToken(user);
        redisTemplate.opsForValue().set(user.getUsername(), jwt, Duration.ofMillis(90000)); // 一分半後過期
        return jwt;
    }

    public MemberPO validateWithJWTToken() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if ("anonymousUser".equals(principal)) {
            throw new ValidationException("你尚未經過身份認證");
        }

        MemberUserDetails userDetails = (MemberUserDetails) principal;
        System.out.printf("嗨，你的帳號：%s%n權限：%s%n",
                userDetails.getUsername(),
                userDetails.getAuthorities());
        return userDetails.getMemberPO();
    }
}
