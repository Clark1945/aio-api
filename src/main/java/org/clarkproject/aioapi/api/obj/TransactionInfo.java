package org.clarkproject.aioapi.api.obj;

import org.clarkproject.aioapi.api.tool.ValidationException;

public class TransactionInfo {
    String account;
    int amount;
    String description;

    public static void depositCheck(TransactionInfo transactionInfo) throws ValidationException {
        if (transactionInfo.getAccount() == null || transactionInfo.getAccount().isEmpty()) {
            throw new ValidationException("Account cannot be empty");
        }
        if (transactionInfo.getAmount() <= 0) {
            throw new ValidationException("Amount must be greater than 0");
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
}