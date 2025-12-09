-- V2: Constraints adicionales y optimizaciones
-- MySQL 8.0 compatible

-- Agregar constraint para validar que afiliados con solicitudes estén activos
-- (Esto se maneja en la lógica de negocio, pero documentamos la intención)

-- Comentarios en las tablas (MySQL syntax)
ALTER TABLE affiliates COMMENT = 'Tabla de afiliados de la cooperativa';
ALTER TABLE users COMMENT = 'Tabla de usuarios del sistema con autenticación JWT';
ALTER TABLE risk_evaluations COMMENT = 'Evaluaciones de riesgo crediticio realizadas';
ALTER TABLE credit_applications COMMENT = 'Solicitudes de crédito de los afiliados';

-- Comentarios en columnas importantes (MySQL syntax)
ALTER TABLE affiliates MODIFY COLUMN estado VARCHAR(20) NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO')) COMMENT 'Estado del afiliado: ACTIVO o INACTIVO';
ALTER TABLE credit_applications MODIFY COLUMN estado VARCHAR(20) NOT NULL CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO')) COMMENT 'Estado de la solicitud: PENDIENTE, APROBADO o RECHAZADO';
ALTER TABLE risk_evaluations MODIFY COLUMN nivel_riesgo VARCHAR(20) NOT NULL CHECK (nivel_riesgo IN ('BAJO', 'MEDIO', 'ALTO')) COMMENT 'Nivel de riesgo: BAJO, MEDIO o ALTO';
ALTER TABLE users MODIFY COLUMN role VARCHAR(20) NOT NULL CHECK (role IN ('ROLE_AFILIADO', 'ROLE_ANALISTA', 'ROLE_ADMIN')) COMMENT 'Rol del usuario: ROLE_AFILIADO, ROLE_ANALISTA o ROLE_ADMIN';
