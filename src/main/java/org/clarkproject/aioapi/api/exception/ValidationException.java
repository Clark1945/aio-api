package org.clarkproject.aioapi.api.exception;

/**
 * 自定義Exception
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
