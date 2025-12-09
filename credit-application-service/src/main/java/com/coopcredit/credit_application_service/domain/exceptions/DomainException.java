package com.coopcredit.credit_application_service.domain.exceptions;

/**
 * Excepción base para errores del dominio
 */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
