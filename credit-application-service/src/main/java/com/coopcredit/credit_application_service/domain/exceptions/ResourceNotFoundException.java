package com.coopcredit.credit_application_service.domain.exceptions;

/**
 * Excepción cuando un recurso no es encontrado
 */
public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
