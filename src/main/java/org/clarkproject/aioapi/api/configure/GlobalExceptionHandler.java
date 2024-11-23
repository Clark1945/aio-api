package org.clarkproject.aioapi.api.configure;

import org.clarkproject.aioapi.api.exception.IllegalObjectStatusException;
import org.clarkproject.aioapi.api.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

import static org.clarkproject.aioapi.api.configure.APIErrorCategory.*;
import static org.clarkproject.aioapi.api.configure.APIErrorCode.*;
import static org.clarkproject.aioapi.api.configure.APIErrorMessage.*;

/**
 * 用來處理Global的例外處理，可以把例外在這裡封裝後回傳
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleValidationException(ValidationException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problemDetail.setTitle("VALIDATION ERROR");
        problemDetail.setProperty("errorCategory", GENERIC_LEVEL);
        problemDetail.setProperty("errorCode",INPUT_ERROR_CODE);
        problemDetail.setProperty("errorMessage", INVALID_REQUEST_PARAM_MSG);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler({IllegalObjectStatusException.class})
    @ResponseStatus(HttpStatus.OK)
    public ProblemDetail handleValidationException(IllegalObjectStatusException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.OK, e.getMessage());
        problemDetail.setTitle("INCORRECT STATUS");
        problemDetail.setProperty("errorCategory", GENERIC_LEVEL);
        problemDetail.setProperty("errorCode",INCORRECT_ACCOUNT_ERROR_CODE);
        problemDetail.setProperty("message", INCORRECT_OPERATION_MSG);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(ResponseStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handlePreconditionException(ResponseStatusException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problemDetail.setTitle("OPERATION TIMEOUT");
        problemDetail.setProperty("errorCategory", SEVERE_LEVEL);
        problemDetail.setProperty("errorCode", UNKNOWN_ERROR_CODE);
        problemDetail.setProperty("message", "unknown error happened. Please contact the maintainer");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
