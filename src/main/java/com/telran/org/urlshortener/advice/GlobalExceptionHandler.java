package com.telran.org.urlshortener.advice;

import com.telran.org.urlshortener.dto.ErrorResponse;
import com.telran.org.urlshortener.exception.EmailNotUniqueException;
import com.telran.org.urlshortener.exception.IdNotFoundException;
import com.telran.org.urlshortener.exception.PathPrefixNotAvailableException;
import com.telran.org.urlshortener.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({IdNotFoundException.class, UserNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        logger.info("Not found: {} for path {}", ex.getMessage(), req.getRequestURI());
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND, req.getRequestURI());
    }

    @ExceptionHandler({EmailNotUniqueException.class, PathPrefixNotAvailableException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest req) {
        logger.warn("Conflict: {} for path {}", ex.getMessage(), req.getRequestURI());
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT, req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return buildResponse(message, HttpStatus.BAD_REQUEST, req.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(HttpMessageNotReadableException ex,
                                                          HttpServletRequest req) {
        return buildResponse("Malformed JSON request", HttpStatus.BAD_REQUEST, req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        logger.error("Unhandled exception for path {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return buildResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR, req.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status, String path) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
        return ResponseEntity.status(status).body(body);
    }
}