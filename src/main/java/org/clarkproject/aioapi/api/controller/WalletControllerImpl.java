package org.clarkproject.aioapi.api.controller;

import org.clarkproject.aioapi.api.obj.enums.ResponseStatusMessage;
import org.clarkproject.aioapi.api.obj.dto.TransactionInfo;
import org.clarkproject.aioapi.api.obj.po.WalletTransactionPO;
import org.clarkproject.aioapi.api.service.WalletService;
import org.clarkproject.aioapi.api.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/1.0")
public class WalletControllerImpl implements WalletController{

    private final WalletService walletService;

    public WalletControllerImpl(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/wallet")
    public ResponseEntity openWallet(@RequestBody HashMap<String, String> reqMap) {
        String account = reqMap.get("account");
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "account is null");
        }

        try {
            boolean isOpen = walletService.openWallet(account);
            if (isOpen) {
                HashMap<String, String> result = new HashMap<>();
                result.put("status", ResponseStatusMessage.SUCCESS.getValue());
                result.put("message", "Wallet open successfully");
                return ResponseEntity
                        .ok()
                        .body(result);
            } else {
                HashMap<String, String> error = new HashMap<>();
                error.put("status", ResponseStatusMessage.ERROR.getValue());
                error.put("message", "Wallt open Fail!");
                return ResponseEntity
                        .badRequest()
                        .body(error);
            }
        } catch (ValidationException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/wallet")
    public ResponseEntity checkWalletBalance(@RequestBody HashMap<String, String> reqMap) {
        String account = reqMap.get("account");
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "account is null");
        }
        try {
            BigDecimal amt = walletService.queryAccount(account).getAmt().setScale(0);
            HashMap<String, Object> result = new HashMap<>();
            result.put("status", ResponseStatusMessage.SUCCESS.getValue());
            result.put("message", "Wallet query successfully");
            HashMap<String, Object> map = new HashMap<>();
            map.put("balance", amt);
            result.put("info", map);
            return ResponseEntity.ok().body(result);
        } catch (ValidationException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

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
