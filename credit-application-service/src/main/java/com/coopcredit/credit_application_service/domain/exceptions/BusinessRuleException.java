package com.coopcredit.credit_application_service.domain.exceptions;

/**
 * Excepción para violaciones de reglas de negocio
 */
public class BusinessRuleException extends DomainException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
