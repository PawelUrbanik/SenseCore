package pl.pawel.sensecore.query.api.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler({
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        return buildWarnResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return buildWarnResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        return buildWarnResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("Unhandled error: status={}, path={}", status.value(), request.getRequestURI(), ex);
        return ResponseEntity.status(status).body(toBody(status, "Internal server error", request.getRequestURI()));
    }

    private ResponseEntity<ApiErrorResponse> buildWarnResponse(HttpStatus status, String message, HttpServletRequest request) {
        String resolvedMessage = (message == null || message.isBlank()) ? status.getReasonPhrase() : message;
        log.warn("Request rejected: status={}, path={}, message={}", status.value(), request.getRequestURI(), resolvedMessage);
        return ResponseEntity.status(status).body(toBody(status, resolvedMessage, request.getRequestURI()));
    }

    private ApiErrorResponse toBody(HttpStatus status, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path);
    }

    public record ApiErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path
    ) {}
}
