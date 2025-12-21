package com.mmd.marcobrico.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
            return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request, null);
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
            return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request, null);
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiError> handleBusiness(BusinessException ex, HttpServletRequest request) {
            return buildResponse(ex.getMessage(), HttpStatus.CONFLICT, request, null);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
            Map<String, String> errors = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

            return buildResponse("Validation failed", HttpStatus.BAD_REQUEST, request, errors);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
            return buildResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR, request, ex.getMessage());
        }

        private ResponseEntity<ApiError> buildResponse(String message, HttpStatus status, HttpServletRequest request, Object details) {
            ApiError error = new ApiError(
                    status.value(),
                    status.getReasonPhrase(),
                    message,
                    request.getRequestURI(),
                    LocalDateTime.now(),
                    details
            );
            return new ResponseEntity<>(error, status);
        }
}
