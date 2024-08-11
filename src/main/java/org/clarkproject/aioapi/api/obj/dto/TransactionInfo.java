package org.clarkproject.aioapi.api.obj.dto;

import lombok.Data;
import org.clarkproject.aioapi.api.exception.ValidationException;

/**
 * 交易DTO
 */
@Data
public class TransactionInfo {
    String account;
    int amount;
    String description;
    String targetAccount;

    public static void depositCheck(TransactionInfo transactionInfo) throws ValidationException {
        if (transactionInfo.getAccount() == null || transactionInfo.getAccount().isEmpty()) {
            throw new ValidationException("Account cannot be empty");
        }
        if (transactionInfo.getAmount() <= 0) {
            throw new ValidationException("Amount must be greater than 0");
        }
    }

    public static void transferCheck(TransactionInfo transactionInfo) throws ValidationException {
        depositCheck(transactionInfo);
        if (transactionInfo.getTargetAccount() == null || transactionInfo.getTargetAccount().isEmpty()) {
            throw new ValidationException("ReceiverId cannot be null");
        }
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTargetAccount() {
        return targetAccount;
    }

    public void setTargetAccount(String targetAccount) {
        this.targetAccount = targetAccount;
    }
}
