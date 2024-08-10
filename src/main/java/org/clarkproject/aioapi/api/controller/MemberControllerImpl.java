package org.clarkproject.aioapi.api.controller;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.clarkproject.aioapi.api.obj.LoginResponse;
import org.clarkproject.aioapi.api.obj.Member;
import org.clarkproject.aioapi.api.obj.MemberUserDetails;
import org.clarkproject.aioapi.api.orm.MemberPO;
import org.clarkproject.aioapi.api.obj.ResponseStatusMessage;
import org.clarkproject.aioapi.api.service.JWTService;
import org.clarkproject.aioapi.api.service.MemberService;
import org.clarkproject.aioapi.api.tool.MemberMapper;
import org.clarkproject.aioapi.api.exception.ValidationException;
import org.clarkproject.aioapi.api.tool.UserIdIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/1.0")
public class MemberControllerImpl implements MemberController {

    private static final Logger log = LoggerFactory.getLogger(MemberControllerImpl.class);
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final UserIdIdentity userIdentity;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    public MemberControllerImpl(MemberService memberService,
                                PasswordEncoder passwordEncoder,
                                UserIdIdentity userIdentity,
                                JWTService jwtService,
                                AuthenticationManager authenticationManager) {
        this.memberService = memberService;
        this.passwordEncoder = passwordEncoder;
        this.userIdentity = userIdentity;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping(value = "/member", consumes = {"application/json"})
    public ResponseEntity register(@Valid @RequestBody Member member, HttpServletRequest request) throws ValidationException {
        String accessIp = request.getRemoteAddr();
        Member.registerValidate(member);

        boolean isMemberExisted = memberService.findActiveAccount(member.getAccount()).isPresent();
        if (isMemberExisted) {
            HashMap<String, String> errors = new HashMap<>();
            errors.put("status", ResponseStatusMessage.ERROR.getValue());
            errors.put("message", "Member already exists");
            return ResponseEntity
                    .badRequest()
                    .body(errors);
        }

        member.setIp(accessIp);
        MemberPO memberPO = MemberMapper.INSTANCE.memberToMemberPo(member);
        try {
            memberService.saveMember(memberPO);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        HashMap<String, String> result = new HashMap<>();
        result.put("status", ResponseStatusMessage.SUCCESS.getValue());
        result.put("message", "Member add successfully");
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(result);
    }


    /**
     * Basic Login without Spring Security
     *
     * @param member
     * @param request
     * @return
     * @throws ValidationException
     */
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody Member member, HttpServletRequest request) throws ValidationException {
        String accessIp = request.getRemoteAddr();

        Member.loginValidate(member);
        MemberPO memberPO = memberService.findActiveAccount(member.getAccount())
                .orElseThrow(() -> new ValidationException("account not available"));
        boolean isLoginIPMeet = memberPO.getIp().getHostAddress().equals(accessIp);
        if (!isLoginIPMeet) {
            log.info("Login IP Change! former ip is {},login ip is {}", memberPO.getIp(), member.getIp());
        }
        boolean isPasswordMeet = passwordEncoder.matches(member.getPassword(), memberPO.getPassword());
        try {
            memberService.updateMemberStatus(memberPO, isPasswordMeet);
        } catch (Exception e) {
            log.warn("帳號狀態異常");
            log.error(e.getMessage());
        }
        if (!isPasswordMeet) {
            throw new ValidationException("Authentication fails because of incorrect password.");
        }

        HashMap<String, String> result = new HashMap<>();
        result.put("status", ResponseStatusMessage.SUCCESS.getValue());
        result.put("message", "Member login successfully");
        return ResponseEntity
                .ok()
                .body(result);
//        } else {
//            HashMap<String, String> error = new HashMap<>();
//            error.put("status", ResponseStatusMessage.ERROR.getValue());
//            error.put("message", "Login Fail!");
//            return ResponseEntity
//                    .badRequest()
//                    .body(error);
//        }
    }

    /**
     * 登入成功回傳 HTTP 200, 錯誤則 401
     *
     * @return
     * @throws ValidationException
     */
    @PostMapping("/loginWithBasicToken")
    public ResponseEntity loginWithBasicToken() throws ValidationException {

        if (userIdentity.isAnonymous()) {
            throw new ValidationException("你尚未經過身份認證");
        }

        System.out.printf(
                "嗨，你%n帳號：%s%n信箱：%s%n權限：%s%n",
            userIdentity.getUsername(),
            userIdentity.getEmail(),
            userIdentity.getAuthority()
        );

        MemberPO memberPO = memberService.findActiveAccount(userIdentity.getUsername())
                .orElseThrow(() -> new ValidationException("account not available"));
        try {
            memberService.updateMemberStatus(memberPO, true);
        } catch (Exception e) {
            log.warn("帳號狀態異常");
            log.error(e.getMessage());
        }

        HashMap<String, String> result = new HashMap<>();
        result.put("status", ResponseStatusMessage.SUCCESS.getValue());
        result.put("message", "Member login successfully");
        return ResponseEntity
                .ok()
                .body(result);
    }

    /**
     * 登入成功回傳 HTTP 200, 錯誤則 401
     *
     * @param member
     * @return
     * @throws ValidationException
     */
    @PostMapping(value = "/getJWTToken",produces = "application/json")
    public LoginResponse loginWithJWTToken(@RequestBody Member member) throws ValidationException {
//        Authentication token = new UsernamePasswordAuthenticationToken(
//                member.getAccount(),
//                member.getPassword()
//        );
//        Authentication auth = authenticationManager.authenticate(token);
//        UserDetails user = (UserDetails) auth.getPrincipal();
//
//        String jwt = jwtService.createLoginAccessToken(user);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if ("anonymousUser".equals(principal)) {
            throw new ValidationException("你尚未經過身份認證");
        }

        MemberUserDetails userDetails = (MemberUserDetails) principal;
        System.out.printf("嗨，你的帳號：%s%n權限：%s%n",
                userDetails.getUsername(),
                userDetails.getAuthorities());
        String jwt = jwtService.createLoginAccessToken(userDetails);
        return LoginResponse.of(jwt,userDetails);
    }

    private static final String BEARER_PREFIX = "Bearer ";

    @PostMapping("/loginWithJWTToken")
    public ResponseEntity loginWithJWTToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Map<String, Object> jwtResult;
        String jwt = authorization.substring(BEARER_PREFIX.length());
        try {
            jwtResult = jwtService.parseToken(jwt);
        } catch (JwtException e) {
            throw new BadCredentialsException(e.getMessage(), e);
        }

        HashMap<String, String> result = new HashMap<>();
        result.put("status", ResponseStatusMessage.SUCCESS.getValue());
        result.put("message", "Member login successfully");
        return ResponseEntity
                .ok()
                .body(result);
    }

    @GetMapping("/member")
    public ResponseEntity getMember(@RequestParam("id") Long id) {
        Optional<MemberPO> memberPO = memberService.findAccountById(id);
        if (memberPO.isPresent()) {
            Member member = MemberMapper.INSTANCE.memberPOToMember(memberPO.get());
            HashMap<String, Object> result = new HashMap<>();
            result.put("status", ResponseStatusMessage.SUCCESS.getValue());
            result.put("message", "Member get successfully");
            result.put("info", member);
            return ResponseEntity.ok().body(result);
        } else {
            HashMap<String, Object> error = new HashMap<>();
            error.put("status", ResponseStatusMessage.ERROR.getValue());
            error.put("message", "Member not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/member")
    public ResponseEntity updateMember(@RequestBody Member member, HttpServletRequest request) throws ValidationException {
        String accessIp = request.getRemoteAddr();
        Member.updateValidate(member);
        boolean isUpdated = memberService.update(member, accessIp);
        if (isUpdated) {
            HashMap<String, String> result = new HashMap<>();
            result.put("status", ResponseStatusMessage.SUCCESS.getValue());
            result.put("message", "Member update successfully");
            return ResponseEntity
                    .ok()
                    .body(result);
        } else {
            HashMap<String, String> error = new HashMap<>();
            error.put("status", ResponseStatusMessage.ERROR.getValue());
            error.put("message", "Member update fail!");
            return ResponseEntity
                    .badRequest()
                    .body(error);
        }
    }

    @PatchMapping("/member")
    public ResponseEntity disableMember(@RequestParam Long id, HttpServletRequest request) throws ValidationException {
        String accessIp = request.getRemoteAddr();
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id is null");
        }
        boolean isDisableSuccess = memberService.disableMember(id, accessIp);
        if (isDisableSuccess) {
            HashMap<String, String> result = new HashMap<>();
            result.put("status", ResponseStatusMessage.SUCCESS.getValue());
            result.put("message", "Member delete successfully");
            return ResponseEntity
                    .ok()
                    .body(result);
        } else {
            HashMap<String, String> error = new HashMap<>();
            error.put("status", ResponseStatusMessage.ERROR.getValue());
            error.put("message", "Member delete fail!");
            return ResponseEntity
                    .badRequest()
                    .body(error);
        }
    }

    @DeleteMapping("/member")
    public ResponseEntity frozeMember(@RequestBody HashMap<String, Long> reqMap,
                                      HttpServletRequest request) throws ValidationException {
        String accessIp = request.getRemoteAddr();
        Long id = reqMap.get("id");
        Long adminId = reqMap.get("adminId");
        if (id == null || adminId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id is null");
        }
        boolean isDisableSuccess = memberService.frozeMember(id, accessIp, adminId);
        if (isDisableSuccess) {
            HashMap<String, String> result = new HashMap<>();
            result.put("status", ResponseStatusMessage.SUCCESS.getValue());
            result.put("message", "Member delete successfully");
            return ResponseEntity
                    .ok()
                    .body(result);
        } else {
            HashMap<String, String> error = new HashMap<>();
            error.put("status", ResponseStatusMessage.ERROR.getValue());
            error.put("message", "Member delete fail!");
            return ResponseEntity
                    .badRequest()
                    .body(error);
        }
    }
}
