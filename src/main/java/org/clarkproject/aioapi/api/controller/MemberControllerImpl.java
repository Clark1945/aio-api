package org.clarkproject.aioapi.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.clarkproject.aioapi.api.exception.IllegalObjectStatusException;
import org.clarkproject.aioapi.api.obj.dto.APIResponse;
import org.clarkproject.aioapi.api.obj.dto.LoginResponse;
import org.clarkproject.aioapi.api.obj.dto.LoginObject;
import org.clarkproject.aioapi.api.obj.dto.Member;
import org.clarkproject.aioapi.api.obj.MemberUserDetails;
import org.clarkproject.aioapi.api.obj.enums.MemberStatus;
import org.clarkproject.aioapi.api.obj.po.MemberPO;
import org.clarkproject.aioapi.api.obj.enums.ResponseStatusMessage;
import org.clarkproject.aioapi.api.service.JWTService;
import org.clarkproject.aioapi.api.service.MemberService;
import org.clarkproject.aioapi.api.tool.MemberMapper;
import org.clarkproject.aioapi.api.exception.ValidationException;
import org.clarkproject.aioapi.api.tool.UserIdIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
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

import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/1.0")
@Slf4j
public class MemberControllerImpl implements MemberController {

    private final MemberService memberService;

    public MemberControllerImpl(MemberService memberService) {
        this.memberService = memberService;
    }

    //TODO 密碼加密存放DB
    //TODO 二階段 認證
    //TODO 密碼複雜度驗證
    @PostMapping(value = "/member", consumes = {"application/json"})
    public ResponseEntity<APIResponse> register(@Valid @RequestBody Member member, HttpServletRequest request) throws ValidationException, IllegalObjectStatusException {
        String accessIp = request.getRemoteAddr();
        Member.registerValidate(member);
        APIResponse apiResponse;

        try {
            memberService.register(member, accessIp);
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
    //TODO 若二階段登入可提供Token
    @PostMapping("/login")
    public ResponseEntity<APIResponse> login(@RequestBody LoginObject member, HttpServletRequest request) throws ValidationException, IllegalObjectStatusException {
        String accessIp = request.getRemoteAddr();
        Member.loginValidate(member);
        APIResponse apiResponse;
        boolean isPasswordMeet;

        MemberPO memberPO = memberService.findActiveAccount(member.getAccount())
                .orElseThrow(() -> new IllegalObjectStatusException("account not available"));

        try {
            isPasswordMeet = memberService.login(memberPO, accessIp, member.getPassword());
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

        String userName = memberService.validateWithBasicToken();
        MemberPO memberPO = memberService.findActiveAccount(userName)
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
    public ResponseEntity<LoginResponse> getJWTToken(@RequestBody Member member) throws IllegalObjectStatusException {
        MemberUserDetails user = memberService.getJWTMember(member);
        MemberPO memberPO = user.getMemberPO();

        if (memberPO == null || !memberPO.getStatus().equals(MemberStatus.ACTIVE.name())) {
            throw new IllegalObjectStatusException("account not available");
        }

        String jwt = memberService.getJWTToken(user);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(LoginResponse.of(jwt, user));
    }

    @PostMapping("/loginWithJWTToken")
    public ResponseEntity<APIResponse> loginWithJWTToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) throws ValidationException {
        memberService.validateWithJWTToken();
        APIResponse apiResponse = new APIResponse(ResponseStatusMessage.SUCCESS.getValue(), "Login successful");
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(apiResponse);
    }

    @GetMapping("/member")
    public ResponseEntity<APIResponse> getMember(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        MemberPO memberPO = memberService.validateWithJWTToken();

        APIResponse apiResponse;
        if (memberPO != null && memberPO.getStatus().equals(MemberStatus.ACTIVE.name())) {
            Member member = MemberMapper.memberPOToMember(memberPO);
            apiResponse = new APIResponse(ResponseStatusMessage.SUCCESS.getValue(), "successfully find member", member);
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        } else {
            apiResponse = new APIResponse(ResponseStatusMessage.ERROR.getValue(), "Member not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
    }

    @PutMapping("/member")
    public ResponseEntity<APIResponse> updateMember(@RequestBody Member newMember,HttpServletRequest request) throws ValidationException, IllegalObjectStatusException {
        String accessIp = request.getRemoteAddr();
        APIResponse apiResponse;
        MemberPO memberPO = memberService.validateWithJWTToken();

        boolean updateResult = false;
        try {
            memberService.update(memberPO, newMember,accessIp);
            updateResult = true;
        }catch (Exception e) {
            e.printStackTrace();
        }
        if (updateResult) {
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
    public ResponseEntity<APIResponse> disableMember(HttpServletRequest request) throws ValidationException, IllegalObjectStatusException {
        String accessIp = request.getRemoteAddr();
        APIResponse apiResponse;
        MemberPO memberPO = memberService.validateWithJWTToken();

        boolean isDisableSuccess = memberService.disableMember(memberPO, accessIp);
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
    public ResponseEntity<APIResponse> frozeMember(String account,
                                                   HttpServletRequest request) throws ValidationException, IllegalObjectStatusException {

        APIResponse apiResponse;
        memberService.validateWithJWTToken();

        if (account == null ) {
            throw new ValidationException("account is null");
        }
        boolean isDisableSuccess = memberService.frozeMember(account);
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
