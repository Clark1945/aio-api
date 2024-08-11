package org.clarkproject.aioapi.api.exception;

/**
 * 驗證錯誤，值若是為空，或是登入驗證失敗皆回傳此錯誤
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
