package io.getarrays.securecapita.exception;

import io.getarrays.securecapita.domain.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class HandleException extends ResponseEntityExceptionHandler implements ErrorController {

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        return new ResponseEntity<>(
                        HttpResponse.builder()
                                .timeStamp(now().toString())
                                .reason(exception.getMessage())
                                .developerMessage(exception.getMessage())
                                .status(HttpStatus.resolve(statusCode.value()))
                                .statusCode(statusCode.value())
                                .build(), statusCode);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        List<FieldError> fieldErrorList = exception.getBindingResult().getFieldErrors();
        String filedMessage = fieldErrorList.stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(",  "));
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason(filedMessage)
                        .developerMessage(exception.getMessage())
                        .status(HttpStatus.resolve(statusCode.value()))
                        .statusCode(statusCode.value())
                        .build(), statusCode);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<HttpResponse> sQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason(exception.getMessage().contains("Duplicate entry") ? "Information already exists" : exception.getMessage())
                        .developerMessage(exception.getMessage())
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<HttpResponse> badCredentialsException(BadCredentialsException exception) {
        log.error(exception.getMessage());

        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason(exception.getMessage() +" "+ ", Incorrect email or password")
                        .developerMessage(exception.getMessage())
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponse> accessDeniedException(AccessDeniedException exception) {
        log.error(exception.getMessage());

        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("Access denied. You don\'t have access")
                        .developerMessage(exception.getMessage())
                        .status(FORBIDDEN)
                        .statusCode(FORBIDDEN.value())
                        .build(), FORBIDDEN);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponse> exception(Exception exception) {
        log.error(exception.getMessage());

        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason(exception.getMessage().contains("except 1, actual 0") ? "Record not found" : exception.getMessage())
                        .developerMessage(exception.getMessage())
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<HttpResponse> emptyResultDataAccessException(EmptyResultDataAccessException exception) {
        log.error(exception.getMessage());

        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason(exception.getMessage().contains("except 1, actual 0") ? "Record not found" : exception.getMessage())
                        .developerMessage(exception.getMessage())
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
    }


    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponse> disabledException(DisabledException exception) {
        log.error(exception.getMessage());

        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .developerMessage(exception.getMessage())
                        //.reason(exception.getMessage().contains("except 1, actual 0") ? "Record not found" : exception.getMessage())
                        .reason("User account is currently disabled")
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<HttpResponse> lockedException(LockedException exception) {
        log.error(exception.getMessage());

        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .developerMessage(exception.getMessage())
                        //.reason(exception.getMessage().contains("except 1, actual 0") ? "Record not found" : exception.getMessage())
                        .reason("User account is currently locked")
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
    }

}
