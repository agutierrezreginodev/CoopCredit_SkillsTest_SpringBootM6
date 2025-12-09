package com.coopcredit.credit_application_service.domain.model;

import com.coopcredit.credit_application_service.domain.enums.UserRole;
import com.coopcredit.credit_application_service.domain.exceptions.BusinessRuleException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo de dominio puro: Usuario
 * Sin dependencias de frameworks
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    private Long id;
    private String username;
    private String password;
    private UserRole role;
    private String documento; // Referencia al documento del afiliado si aplica

    /**
     * Valida las reglas de negocio del usuario
     */
    public void validate() {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessRuleException("El nombre de usuario es obligatorio");
        }
        
        if (username.length() < 3) {
            throw new BusinessRuleException("El nombre de usuario debe tener al menos 3 caracteres");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessRuleException("La contraseña es obligatoria");
        }
        
        if (role == null) {
            throw new BusinessRuleException("El rol del usuario es obligatorio");
        }
    }

    /**
     * Verifica si el usuario es un afiliado
     */
    public boolean isAffiliate() {
        return UserRole.ROLE_AFILIADO.equals(this.role);
    }

    /**
     * Verifica si el usuario es un analista
     */
    public boolean isAnalyst() {
        return UserRole.ROLE_ANALISTA.equals(this.role);
    }

    /**
     * Verifica si el usuario es un administrador
     */
    public boolean isAdmin() {
        return UserRole.ROLE_ADMIN.equals(this.role);
    }
}
