package org.clarkproject.aioapi.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.clarkproject.aioapi.api.exception.IllegalObjectStatusException;
import org.clarkproject.aioapi.api.obj.APIResponse;
import org.clarkproject.aioapi.api.obj.LoginResponse;
import org.clarkproject.aioapi.api.obj.dto.LoginObject;
import org.clarkproject.aioapi.api.obj.dto.Member;
import org.clarkproject.aioapi.api.obj.MemberUserDetails;
import org.clarkproject.aioapi.api.obj.enums.MemberStatus;
import org.clarkproject.aioapi.api.obj.po.MemberPO;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
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
    public ResponseEntity<APIResponse> register(@Valid @RequestBody Member member, HttpServletRequest request) throws ValidationException {
        String accessIp = request.getRemoteAddr();
        Member.registerValidate(member);
        APIResponse apiResponse;

        boolean isMemberExisted = memberService.findActiveAccount(member.getAccount()).isPresent();
        if (isMemberExisted) {
            apiResponse = new APIResponse(ResponseStatusMessage.ERROR.getValue(), "Member already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(apiResponse);
        }

        MemberPO memberPO = MemberMapper.INSTANCE.memberToMemberPo(member);
        memberPO.setIp(MemberService.stringToInetAddress(accessIp));
        try {
            memberService.saveMember(memberPO);
        } catch (Exception e) {
            e.printStackTrace();
            apiResponse = new APIResponse(ResponseStatusMessage.ERROR.getValue(), "Failed to register member");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(apiResponse);
        }
        apiResponse = new APIResponse(ResponseStatusMessage.SUCCESS.getValue(), "Member add successfully");
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(apiResponse);
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
    public ResponseEntity<APIResponse> login(@RequestBody LoginObject member, HttpServletRequest request) throws ValidationException, IllegalObjectStatusException {
        String accessIp = request.getRemoteAddr();
        Member.loginValidate(member);
        APIResponse apiResponse;

        MemberPO memberPO = memberService.findActiveAccount(member.getAccount())
                .orElseThrow(() -> new IllegalObjectStatusException("account not available"));
        boolean isLoginIPMeet = memberPO.getIp().getHostAddress().equals(accessIp);
        if (!isLoginIPMeet) {
            log.info("Login IP Change! former ip is {},login ip is {}", memberPO.getIp(), accessIp);
        }

        boolean isPasswordMeet = passwordEncoder.matches(member.getPassword(), memberPO.getPassword());
        try {
            memberService.updateMemberStatus(memberPO, isPasswordMeet);
        } catch (Exception e) {
            e.printStackTrace();
            apiResponse = new APIResponse(ResponseStatusMessage.ERROR.getValue(), "Failed to login member");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(apiResponse);
        }
        if (!isPasswordMeet) {
            apiResponse = new APIResponse(ResponseStatusMessage.ERROR.getValue(), "Password does not match");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(apiResponse);
        }

        apiResponse = new APIResponse(ResponseStatusMessage.SUCCESS.getValue(), "Login successful");
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(apiResponse);
    }

    /**
     * 以HTTP BASIC TOKEN登入
     *
     * @return
     * @throws ValidationException
     */
    @PostMapping("/loginWithBasicToken")
    public ResponseEntity<APIResponse> loginWithBasicToken() throws ValidationException, IllegalObjectStatusException {
        APIResponse apiResponse;

        if (userIdentity.isAnonymous()) {
            throw new ValidationException("你尚未經過身份認證");
        }

        System.out.printf(
                "你的帳號：%s%n信箱：%s%n權限：%s%n",
                userIdentity.getUsername(),
                userIdentity.getEmail(),
                userIdentity.getAuthority()
        );

        MemberPO memberPO = memberService.findActiveAccount(userIdentity.getUsername())
                .orElseThrow(() -> new IllegalObjectStatusException("account not available"));
        try {
            memberService.updateMemberStatus(memberPO, true);
        } catch (Exception e) {
            e.printStackTrace();
            apiResponse = new APIResponse(ResponseStatusMessage.ERROR.getValue(), "Failed to login member");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(apiResponse);
        }

        apiResponse = new APIResponse(ResponseStatusMessage.SUCCESS.getValue(), "Login successful");

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(apiResponse);
    }

    /**
     * 登入成功回傳 HTTP 200, 錯誤則 401
     *
     * @param member
     * @return
     * @throws ValidationException
     */
    @PostMapping(value = "/getJWTToken", produces = "application/json")
    public ResponseEntity<LoginResponse> loginWithJWTToken(@RequestBody Member member) throws IllegalObjectStatusException {
        Authentication token = new UsernamePasswordAuthenticationToken(
                member.getAccount(),
                member.getPassword()
        );
        Authentication auth = authenticationManager.authenticate(token);
        UserDetails user = (UserDetails) auth.getPrincipal();

        MemberPO memberPO = ((MemberUserDetails) user).getMemberPO();
        if (memberPO == null || !memberPO.getStatus().equals(MemberStatus.ACTIVE.name())) {
            throw new IllegalObjectStatusException("account not available");
        }

        String jwt = jwtService.createLoginAccessToken(user);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(LoginResponse.of(jwt, user));
    }

    @PostMapping("/loginWithJWTToken")
    public ResponseEntity<APIResponse> loginWithJWTToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) throws ValidationException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if ("anonymousUser".equals(principal)) {
            throw new ValidationException("你尚未經過身份認證");
        }

        MemberUserDetails userDetails = (MemberUserDetails) principal;
        System.out.printf("嗨，你的帳號：%s%n權限：%s%n",
                userDetails.getUsername(),
                userDetails.getAuthorities());
//        改由filter實作了
//        Map<String, Object> jwtResult;
//        String jwt = authorization.substring(BEARER_PREFIX.length());
//        try {
//            jwtResult = jwtService.parseToken(jwt);
//        } catch (JwtException e) {
//            throw new BadCredentialsException(e.getMessage(), e);
//        }
        APIResponse apiResponse = new APIResponse(ResponseStatusMessage.SUCCESS.getValue(), "Login successful");
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(apiResponse);
    }

    @GetMapping("/member")
    public ResponseEntity<APIResponse> getMember(@RequestParam("id") Long id) {
        APIResponse apiResponse;
        Optional<MemberPO> memberPO = memberService.findAccountById(id);
        if (memberPO.isPresent()) {
            Member member = MemberMapper.INSTANCE.memberPOToMember(memberPO.get());
            apiResponse = new APIResponse(ResponseStatusMessage.SUCCESS.getValue(), "successfully find member", member);
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        } else {
            apiResponse = new APIResponse(ResponseStatusMessage.ERROR.getValue(), "Member not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
    }

    @PutMapping("/member")
    public ResponseEntity<APIResponse> updateMember(@RequestBody Member member, HttpServletRequest request) throws ValidationException, IllegalObjectStatusException {
        String accessIp = request.getRemoteAddr();
        APIResponse apiResponse;
        Member.updateValidate(member);

        boolean isUpdated = memberService.update(member, accessIp);
        if (isUpdated) {
            apiResponse = new APIResponse(ResponseStatusMessage.SUCCESS.getValue(), "Member successfully updated");
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body(apiResponse);
        } else {
            apiResponse = new APIResponse(ResponseStatusMessage.ERROR.getValue(), "Member update fail");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(apiResponse);
        }
    }

    @PatchMapping("/member")
    public ResponseEntity<APIResponse> disableMember(@RequestParam Long id, HttpServletRequest request) throws ValidationException, IllegalObjectStatusException {
        String accessIp = request.getRemoteAddr();
        APIResponse apiResponse;

        if (id == null) {
            throw new ValidationException("id is null");
        }
        boolean isDisableSuccess = memberService.disableMember(id, accessIp);
        if (isDisableSuccess) {
            apiResponse = new APIResponse(ResponseStatusMessage.SUCCESS.getValue(), "Member disabled");
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body(apiResponse);
        } else {
            apiResponse = new APIResponse(ResponseStatusMessage.ERROR.getValue(), "Member disabled fail");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(apiResponse);
        }
    }

    @DeleteMapping("/member")
    public ResponseEntity<APIResponse> frozeMember(@RequestBody HashMap<String, Long> reqMap,
                                                   HttpServletRequest request) throws ValidationException, IllegalObjectStatusException {
        String accessIp = request.getRemoteAddr();
        Long id = reqMap.get("id");
        Long adminId = reqMap.get("adminId");
        APIResponse apiResponse;

        if (id == null || adminId == null) {
            throw new ValidationException("id is null");
        }
        boolean isDisableSuccess = memberService.frozeMember(id, accessIp, adminId);
        if (isDisableSuccess) {
            apiResponse = new APIResponse(ResponseStatusMessage.SUCCESS.getValue(), "Member frozed");
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body(apiResponse);
        } else {
            apiResponse = new APIResponse(ResponseStatusMessage.ERROR.getValue(), "Member frozed fail");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(apiResponse);
        }
    }
}
