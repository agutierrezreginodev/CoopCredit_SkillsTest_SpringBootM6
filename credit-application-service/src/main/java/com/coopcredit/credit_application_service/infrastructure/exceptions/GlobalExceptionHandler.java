package com.coopcredit.credit_application_service.infrastructure.exceptions;

import com.coopcredit.credit_application_service.domain.exceptions.BusinessRuleException;
import com.coopcredit.credit_application_service.domain.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manejador Global de Excepciones
 * Implementa RFC 7807 (Problem Details for HTTP APIs)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        String traceId = UUID.randomUUID().toString();
        logger.error("[{}] Error de validación: {}", traceId, ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Error de validación en los datos de entrada"
        );
        
        problemDetail.setType(URI.create("https://coopcredit.com/errors/validation-error"));
        problemDetail.setTitle("Validation Error");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("traceId", traceId);
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        problemDetail.setProperty("errors", errors);
        
        return problemDetail;
    }

    /**
     * Maneja excepciones de recurso no encontrado
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request) {
        
        String traceId = UUID.randomUUID().toString();
        logger.error("[{}] Recurso no encontrado: {}", traceId, ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        
        problemDetail.setType(URI.create("https://coopcredit.com/errors/resource-not-found"));
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("traceId", traceId);
        
        return problemDetail;
    }

    /**
     * Maneja excepciones de reglas de negocio
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRuleException(
            BusinessRuleException ex,
            WebRequest request) {
        
        String traceId = UUID.randomUUID().toString();
        logger.error("[{}] Violación de regla de negocio: {}", traceId, ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage()
        );
        
        problemDetail.setType(URI.create("https://coopcredit.com/errors/business-rule-violation"));
        problemDetail.setTitle("Business Rule Violation");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("traceId", traceId);
        
        return problemDetail;
    }

    /**
     * Maneja excepciones de acceso denegado
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {
        
        String traceId = UUID.randomUUID().toString();
        logger.error("[{}] Acceso denegado: {}", traceId, ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "No tiene permisos para acceder a este recurso"
        );
        
        problemDetail.setType(URI.create("https://coopcredit.com/errors/access-denied"));
        problemDetail.setTitle("Access Denied");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("traceId", traceId);
        
        return problemDetail;
    }

    /**
     * Maneja excepciones generales
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGlobalException(
            Exception ex,
            WebRequest request) {
        
        String traceId = UUID.randomUUID().toString();
        logger.error("[{}] Error interno del servidor: {}", traceId, ex.getMessage(), ex);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ha ocurrido un error interno en el servidor"
        );
        
        problemDetail.setType(URI.create("https://coopcredit.com/errors/internal-server-error"));
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("message", ex.getMessage());
        
        return problemDetail;
    }

    /**
     * Maneja excepciones de runtime generales
     */
    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException(
            RuntimeException ex,
            WebRequest request) {
        
        String traceId = UUID.randomUUID().toString();
        logger.error("[{}] Error de ejecución: {}", traceId, ex.getMessage(), ex);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        
        problemDetail.setType(URI.create("https://coopcredit.com/errors/runtime-error"));
        problemDetail.setTitle("Runtime Error");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("traceId", traceId);
        
        return problemDetail;
    }
}
