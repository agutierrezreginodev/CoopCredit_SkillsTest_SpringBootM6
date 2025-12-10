-- V2: Constraints adicionales y optimizaciones
-- PostgreSQL 18 compatible

-- Agregar constraint para validar que afiliados con solicitudes estén activos
-- (Esto se maneja en la lógica de negocio, pero documentamos la intención)

-- Comentarios en las tablas (PostgreSQL syntax)
COMMENT ON TABLE affiliates IS 'Tabla de afiliados de la cooperativa';
COMMENT ON TABLE users IS 'Tabla de usuarios del sistema con autenticación JWT';
COMMENT ON TABLE risk_evaluations IS 'Evaluaciones de riesgo crediticio realizadas';
COMMENT ON TABLE credit_applications IS 'Solicitudes de crédito de los afiliados';

-- Comentarios en columnas importantes (PostgreSQL syntax)
COMMENT ON COLUMN affiliates.estado IS 'Estado del afiliado: ACTIVO o INACTIVO';
COMMENT ON COLUMN credit_applications.estado IS 'Estado de la solicitud: PENDIENTE, APROBADO o RECHAZADO';
COMMENT ON COLUMN risk_evaluations.nivel_riesgo IS 'Nivel de riesgo: BAJO, MEDIO o ALTO';
COMMENT ON COLUMN users.role IS 'Rol del usuario: ROLE_AFILIADO, ROLE_ANALISTA o ROLE_ADMIN';
