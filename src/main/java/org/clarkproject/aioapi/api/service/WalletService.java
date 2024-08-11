package org.clarkproject.aioapi.api.service;

import org.clarkproject.aioapi.api.obj.dto.TransactionInfo;
import org.clarkproject.aioapi.api.obj.enums.*;
import org.clarkproject.aioapi.api.obj.po.MemberPO;
import org.clarkproject.aioapi.api.obj.po.WalletPO;
import org.clarkproject.aioapi.api.obj.po.WalletTransactionPO;
import org.clarkproject.aioapi.api.repository.WalletRepository;
import org.clarkproject.aioapi.api.repository.WalletTransactionRepository;
import org.clarkproject.aioapi.api.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service("WalletService")
public class WalletService {

    private final MemberService memberService;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public WalletService(MemberService memberService,
                         WalletRepository walletRepository,
                         WalletTransactionRepository transactionRepository) {
        this.memberService = memberService;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean openWallet(String account) throws ValidationException {
        MemberPO memberPO = memberService.findActiveAccount(account)
                .orElseThrow(() -> new ValidationException("account not available"));

        WalletPO walletPO = new WalletPO();
        walletPO.setAmt(new BigDecimal(0));
        walletPO.setStatus(WalletStatus.ACTIVE.name());
        try {
            WalletPO walletPO1 = walletRepository.save(walletPO);
            memberPO.setWalletId((walletPO1.getId()));
            memberService.saveMember(memberPO);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public WalletPO queryAccount(String account) throws ValidationException {
        MemberPO memberPO = memberService.findActiveAccount(account)
                .orElseThrow(() -> new ValidationException("account not available"));

        Optional<WalletPO> walletPO = walletRepository.findById(memberPO.getWalletId());
        walletPO.orElseThrow(() -> new ValidationException("wallet not available"));
        walletPO.filter(w -> w.getStatus().equals(WalletStatus.ACTIVE.name()))
                .orElseThrow(() -> new ValidationException("walletPO not active"));
        return walletPO.get();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean deposit(TransactionInfo transactionInfo) throws ValidationException {
        WalletPO walletPO = queryAccount(transactionInfo.getAccount());

        WalletTransactionPO walletTransactionPO = new WalletTransactionPO();
        walletTransactionPO.setWallet(walletPO);
        walletTransactionPO.setCompleteTime(LocalDateTime.now());
        walletTransactionPO.setTxType(TransactionType.DEPOSIT.name());
        walletTransactionPO.setAmt(new BigDecimal(transactionInfo.getAmount()));
        walletTransactionPO.setTransactionStatus(TransactionStatus.COMPLETED.name());
        walletTransactionPO.setTransactionId(UUID.randomUUID());
        walletTransactionPO.setFee(new BigDecimal(0));
        walletTransactionPO.setDescription(transactionInfo.getDescription());

        walletPO.setAmt(walletPO.getAmt().add(new BigDecimal(transactionInfo.getAmount())));
        walletPO.setLastTxTime(LocalDateTime.now());
        try {
            transactionRepository.save(walletTransactionPO);
            walletRepository.save(walletPO);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean withdraw(TransactionInfo transactionInfo) throws ValidationException {
        WalletPO walletPO = queryAccount(transactionInfo.getAccount());
        BigDecimal fee = new BigDecimal(1);

        WalletTransactionPO walletTransactionPO = new WalletTransactionPO();
        walletTransactionPO.setWallet(walletPO);
        walletTransactionPO.setCompleteTime(LocalDateTime.now());
        walletTransactionPO.setTxType(TransactionType.WITHDRAWAL.name());
        walletTransactionPO.setAmt(new BigDecimal(transactionInfo.getAmount()));
        walletTransactionPO.setTransactionStatus(TransactionStatus.COMPLETED.name());
        walletTransactionPO.setTransactionId(UUID.randomUUID());
        walletTransactionPO.setFee(fee); // 酌收手續費
        walletTransactionPO.setDescription(transactionInfo.getDescription());

        walletPO.setAmt(walletPO.getAmt().subtract(new BigDecimal(transactionInfo.getAmount())).subtract(fee));
        walletPO.setLastTxTime(LocalDateTime.now());
        try {
            transactionRepository.save(walletTransactionPO);
            walletRepository.save(walletPO);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean transfer(TransactionInfo transactionInfo) throws ValidationException {
        WalletPO walletPO = queryAccount(transactionInfo.getAccount());
        BigDecimal fee = new BigDecimal(1);

        WalletTransactionPO walletTransactionPO = new WalletTransactionPO();
        walletTransactionPO.setWallet(walletPO);
        walletTransactionPO.setCompleteTime(LocalDateTime.now());
        walletTransactionPO.setTxType(TransactionType.TRANSFER.name());
        walletTransactionPO.setAmt(new BigDecimal(transactionInfo.getAmount()));
        walletTransactionPO.setTransactionStatus(TransactionStatus.COMPLETED.name());
        walletTransactionPO.setTransactionId(UUID.randomUUID());
        walletTransactionPO.setFee(fee); // 酌收手續費
        walletTransactionPO.setDescription(transactionInfo.getDescription());
        WalletPO targetWalletPO = queryAccount(transactionInfo.getTargetAccount());
        walletTransactionPO.setReceiver(targetWalletPO.getId());

        walletPO.setAmt(walletPO.getAmt().subtract(new BigDecimal(transactionInfo.getAmount())).subtract(fee));
        walletPO.setLastTxTime(LocalDateTime.now());
        try {
            transactionRepository.save(walletTransactionPO);
            walletRepository.save(walletPO);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<WalletTransactionPO> getWalletRecord(TransactionInfo transactionInfo) throws ValidationException {
        MemberPO memberPO = memberService.findActiveAccount(transactionInfo.getAccount())
                .orElseThrow(() -> new ValidationException("account not available"));

        WalletPO walletPO = walletRepository.findById(memberPO.getWalletId())
                .orElseThrow(() -> new ValidationException("wallet not found"));
        if (!walletPO.getStatus().equals(MemberStatus.ACTIVE.name())) {
            throw new ValidationException("walletPO not active");
        }

        return transactionRepository.findAllByWalletId(walletPO.getId())
                .orElseThrow(() -> new ValidationException("wallet not found"));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean freeze(TransactionInfo transactionInfo) throws ValidationException {
        MemberPO memberPO = memberService.findActiveAccount(transactionInfo.getAccount())
                .orElseThrow(() -> new ValidationException("account not available"));
        WalletPO walletPO = walletRepository.findById(memberPO.getWalletId())
                .orElseThrow(() -> new ValidationException("wallet not found"));
        walletPO.setStatus(WalletStatus.SUSPENDED.name());

        try {
            walletRepository.save(walletPO);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean deactivate(TransactionInfo transactionInfo) throws ValidationException {
        MemberPO memberPO = memberService.findActiveAccount(transactionInfo.getAccount())
                .filter(m -> m.getRole().equals(MemberRole.ADMIN.name()))
                .orElseThrow(() -> new ValidationException("account not available"));
        WalletPO walletPO = walletRepository.findById(memberPO.getWalletId())
                .orElseThrow(() -> new ValidationException("wallet not found"));
        walletPO.setStatus(WalletStatus.INACTIVE.name());

        try {
            walletRepository.save(walletPO);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
