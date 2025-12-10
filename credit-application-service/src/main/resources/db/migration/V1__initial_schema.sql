-- V1: Esquema inicial de base de datos
-- CoopCredit System - Credit Application Service
-- PostgreSQL 18 compatible

-- Tabla de Afiliados
CREATE TABLE affiliates (
    id BIGSERIAL PRIMARY KEY,
    documento VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    salario DECIMAL(15, 2) NOT NULL CHECK (salario > 0),
    fecha_afiliacion DATE NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    CONSTRAINT uk_affiliate_documento UNIQUE (documento)
);

-- Tabla de Usuarios
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ROLE_AFILIADO', 'ROLE_ANALISTA', 'ROLE_ADMIN')),
    documento VARCHAR(20),
    CONSTRAINT uk_user_username UNIQUE (username)
);

-- Tabla de Evaluaciones de Riesgo
CREATE TABLE risk_evaluations (
    id BIGSERIAL PRIMARY KEY,
    documento VARCHAR(20) NOT NULL,
    score INTEGER NOT NULL CHECK (score >= 300 AND score <= 950),
    nivel_riesgo VARCHAR(20) NOT NULL CHECK (nivel_riesgo IN ('BAJO', 'MEDIO', 'ALTO')),
    detalle TEXT,
    fecha_evaluacion TIMESTAMP NOT NULL
);

-- Tabla de Solicitudes de Crédito
CREATE TABLE credit_applications (
    id BIGSERIAL PRIMARY KEY,
    afiliado_id BIGINT NOT NULL,
    monto_solicitado DECIMAL(15, 2) NOT NULL CHECK (monto_solicitado > 0),
    plazo_meses INTEGER NOT NULL CHECK (plazo_meses > 0 AND plazo_meses <= 120),
    tasa_propuesta DECIMAL(5, 2) NOT NULL CHECK (tasa_propuesta >= 0 AND tasa_propuesta <= 100),
    fecha_solicitud TIMESTAMP NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO')),
    motivo_rechazo TEXT,
    evaluacion_riesgo_id BIGINT,
    CONSTRAINT fk_application_affiliate FOREIGN KEY (afiliado_id) REFERENCES affiliates(id),
    CONSTRAINT fk_application_evaluation FOREIGN KEY (evaluacion_riesgo_id) REFERENCES risk_evaluations(id)
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_affiliate_documento ON affiliates(documento);
CREATE INDEX idx_affiliate_estado ON affiliates(estado);
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_documento ON users(documento);
CREATE INDEX idx_application_afiliado ON credit_applications(afiliado_id);
CREATE INDEX idx_application_estado ON credit_applications(estado);
CREATE INDEX idx_application_fecha ON credit_applications(fecha_solicitud);
CREATE INDEX idx_risk_documento ON risk_evaluations(documento);
