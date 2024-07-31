package org.clarkproject.aioapi.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.clarkproject.aioapi.api.obj.Member;
import org.clarkproject.aioapi.api.orm.MemberPO;
import org.clarkproject.aioapi.api.obj.ResponseStatusMessage;
import org.clarkproject.aioapi.api.service.MemberService;
import org.clarkproject.aioapi.api.tool.MemberMapper;
import org.clarkproject.aioapi.api.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/1.0")
public class MemberController implements IMemberController {

    private static final Logger log = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping(value = "/member",consumes = {"application/json"})
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

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody Member member, HttpServletRequest request) throws ValidationException {
        String accessIp = request.getRemoteAddr();
        Member.loginValidate(member);
        boolean isPass = memberService.login(member, accessIp);

        if (isPass) {
            HashMap<String, String> result = new HashMap<>();
            result.put("status", ResponseStatusMessage.SUCCESS.getValue());
            result.put("message", "Member add successfully");
            return ResponseEntity
                    .ok()
                    .body(result);
        } else {
            HashMap<String, String> error = new HashMap<>();
            error.put("status", ResponseStatusMessage.ERROR.getValue());
            error.put("message", "Login Fail!");
            return ResponseEntity
                    .badRequest()
                    .body(error);
        }
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
            HashMap<String,String> result = new HashMap<>();
            result.put("status",ResponseStatusMessage.SUCCESS.getValue());
            result.put("message","Member delete successfully");
            return ResponseEntity
                    .ok()
                    .body(result);
        } else {
            HashMap<String, String> error = new HashMap<>();
            error.put("status",ResponseStatusMessage.ERROR.getValue());
            error.put("message","Member delete fail!");
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
            HashMap<String,String> result = new HashMap<>();
            result.put("status",ResponseStatusMessage.SUCCESS.getValue());
            result.put("message","Member delete successfully");
            return ResponseEntity
                    .ok()
                    .body(result);
        } else {
            HashMap<String, String> error = new HashMap<>();
            error.put("status",ResponseStatusMessage.ERROR.getValue());
            error.put("message","Member delete fail!");
            return ResponseEntity
                    .badRequest()
                    .body(error);
        }
    }
}
