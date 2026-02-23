package com.github.saydov.documents.exception.advice;

import com.github.saydov.documents.exception.DocumentServiceException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GeneralAdvice extends ResponseEntityExceptionHandler {

    private static final String CODE_PROPERTY = "code";

    @ExceptionHandler(DocumentServiceException.class)
    public ResponseEntity<ProblemDetail> handleDocumentServiceException(DocumentServiceException ex) {
        log.warn("Document service error: {}", ex.getMessage());

        var problem = createProblem(ex.getStatus(), ex.getMessage(), ex.getStatus().name());
        return ResponseEntity.status(ex.getStatus()).body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request
    ) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("Validation failed: {}", errors);

        var problem = createProblem(HttpStatus.BAD_REQUEST, errors.isEmpty()
                ? "Validation failed" : errors, "VALIDATION_ERROR");
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch for parameter '{}': {}", ex.getName(), ex.getValue());
        return createProblem(HttpStatus.BAD_REQUEST,
                "Parameter '%s': invalid type".formatted(ex.getName()), HttpStatus.BAD_REQUEST.name());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return createProblem(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.name());
    }

    private ProblemDetail createProblem(HttpStatus status, String detail, String code) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty(CODE_PROPERTY, code);
        return problem;
    }
}