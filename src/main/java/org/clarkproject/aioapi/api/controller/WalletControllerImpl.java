package org.clarkproject.aioapi.api.controller;

import org.clarkproject.aioapi.api.obj.dto.APIResponse;
import org.clarkproject.aioapi.api.obj.enums.ResponseStatusMessage;
import org.clarkproject.aioapi.api.obj.dto.TransactionInfo;
import org.clarkproject.aioapi.api.obj.po.MemberPO;
import org.clarkproject.aioapi.api.obj.po.WalletTransactionPO;
import org.clarkproject.aioapi.api.service.MemberService;
import org.clarkproject.aioapi.api.service.WalletService;
import org.clarkproject.aioapi.api.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/1.0")
public class WalletControllerImpl implements WalletController{

    private final WalletService walletService;
    private final MemberService memberService;
    public WalletControllerImpl(WalletService walletService, MemberService memberService) {
        this.walletService = walletService;
        this.memberService = memberService;
    }

    @PostMapping("/wallet")
    public ResponseEntity<APIResponse> openWallet() {
        APIResponse apiResponse;
        MemberPO memberPO = memberService.validateWithJWTToken();
        try {
            walletService.openWallet(memberPO);
            apiResponse = new APIResponse(ResponseStatusMessage.SUCCESS.getValue(), "Wallet open successfully");
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        } catch (ValidationException e) {
            e.printStackTrace();
            apiResponse = new APIResponse(ResponseStatusMessage.ERROR.getValue(), "Wallt open Fail!");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(apiResponse);
        }
    }

    @GetMapping("/wallet")
    public ResponseEntity<APIResponse> checkWalletBalance() {
        APIResponse apiResponse;
        MemberPO memberPO = memberService.validateWithJWTToken();
        try {
            BigDecimal amt = walletService.queryAccount(memberPO).getAmt().setScale(0, RoundingMode.CEILING);

            HashMap<String, Object> map = new HashMap<>();
            map.put("balance", amt);
            apiResponse = new APIResponse(ResponseStatusMessage.SUCCESS.getValue(), "Wallet query successfully", map);
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        } catch (ValidationException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    //TODO 以下API待重構
    @PostMapping("/deposit")
    public ResponseEntity deposit(@RequestBody TransactionInfo transactionInfo) {
        try {
            TransactionInfo.depositCheck(transactionInfo);
        } catch (ValidationException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        try {
            boolean isSuccess = walletService.deposit(transactionInfo);
            if (isSuccess) {
                HashMap<String, String> result = new HashMap<>();
                result.put("status", ResponseStatusMessage.SUCCESS.getValue());
                result.put("message", "Wallet Deposit successfully");
                return ResponseEntity
                        .ok()
                        .body(result);
            } else {
                HashMap<String, String> error = new HashMap<>();
                error.put("status", ResponseStatusMessage.ERROR.getValue());
                error.put("message", "Wallet Deposit Fail!");
                return ResponseEntity
                        .badRequest()
                        .body(error);
            }
        } catch (ValidationException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

    }

    @PostMapping("/withdraw")
    public ResponseEntity withdraw(@RequestBody TransactionInfo transactionInfo) {
        try {
            TransactionInfo.depositCheck(transactionInfo);
        } catch (ValidationException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        try {
            boolean isSuccess = walletService.withdraw(transactionInfo);
            if (isSuccess) {
                HashMap<String, String> result = new HashMap<>();
                result.put("status", ResponseStatusMessage.SUCCESS.getValue());
                result.put("message", "Wallet withdraw successfully");
                return ResponseEntity
                        .ok()
                        .body(result);
            } else {
                HashMap<String, String> error = new HashMap<>();
                error.put("status", ResponseStatusMessage.ERROR.getValue());
                error.put("message", "Wallet withdraw Fail!");
                return ResponseEntity
                        .badRequest()
                        .body(error);
            }
        } catch (ValidationException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    //TODO 收款方是否也要加入交易紀錄
    @PostMapping("/transfer")
    public ResponseEntity transfer(@RequestBody TransactionInfo transactionInfo) {
        try {
            TransactionInfo.transferCheck(transactionInfo);
        } catch (ValidationException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        try {
            boolean isSuccess = walletService.transfer(transactionInfo);
            if (isSuccess) {
                HashMap<String, String> result = new HashMap<>();
                result.put("status", ResponseStatusMessage.SUCCESS.getValue());
                result.put("message", "Wallet transfer successfully");
                return ResponseEntity
                        .ok()
                        .body(result);
            } else {
                HashMap<String, String> error = new HashMap<>();
                error.put("status", ResponseStatusMessage.ERROR.getValue());
                error.put("message", "Wallet transfer Fail!");
                return ResponseEntity
                        .badRequest()
                        .body(error);
            }
        } catch (ValidationException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    //TODO 建立DTO不直接回傳PO
    @GetMapping("/wallet_record")
    public ResponseEntity getWalletRecord(@RequestBody TransactionInfo transactionInfo) {
        if (transactionInfo.getAccount() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "account is null");
        }
        try {
            List<WalletTransactionPO> transactionPOList = walletService.getWalletRecord(transactionInfo);
            HashMap<String, Object> result = new HashMap<>();
            result.put("status", ResponseStatusMessage.SUCCESS.getValue());
            result.put("message", "Wallet record successfully");
            result.put("info", transactionPOList);
            return ResponseEntity.ok().body(result);
        } catch (ValidationException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PatchMapping("/wallet")
    public ResponseEntity freeze(@RequestBody TransactionInfo transactionInfo) {
        if (transactionInfo.getAccount() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "account is null");
        }

        try {
            boolean isSuccess = walletService.freeze(transactionInfo);
            if (isSuccess) {
                HashMap<String, String> result = new HashMap<>();
                result.put("status", ResponseStatusMessage.SUCCESS.getValue());
                result.put("message", "Wallet freeze successfully");
                return ResponseEntity.ok().body(result);
            } else {
                HashMap<String, String> error = new HashMap<>();
                error.put("status", ResponseStatusMessage.ERROR.getValue());
                error.put("message", "Wallet freeze Fail!");
                return ResponseEntity.badRequest().body(error);
            }
        } catch (ValidationException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/wallet")
    public ResponseEntity deactivate(@RequestBody TransactionInfo transactionInfo) {
        if (transactionInfo.getAccount() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "account is null");
        }

        try {
            boolean isSuccess = walletService.deactivate(transactionInfo);
            if (isSuccess) {
                HashMap<String, String> result = new HashMap<>();
                result.put("status", ResponseStatusMessage.SUCCESS.getValue());
                result.put("message", "Wallet deactivate successfully");
                return ResponseEntity.ok().body(result);
            } else {
                HashMap<String, String> error = new HashMap<>();
                error.put("status", ResponseStatusMessage.ERROR.getValue());
                error.put("message", "Wallet deactivate Fail!");
                return ResponseEntity.badRequest().body(error);
            }
        } catch (ValidationException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


}
