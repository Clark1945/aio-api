package org.clarkproject.aioapi.api.exception;

/**
 * 資料為不正確型態，比方說查無資料或資料狀態不正確
 */
public class IllegalObjectStatusException extends RuntimeException {
    public IllegalObjectStatusException(String message) {
        super(message);
    }
}
