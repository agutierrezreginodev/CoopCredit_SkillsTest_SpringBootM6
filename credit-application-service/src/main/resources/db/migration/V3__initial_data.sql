-- V3: Datos iniciales para pruebas

-- Usuario administrador (password: admin123)
INSERT INTO users (username, password, role, documento) VALUES 
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_ADMIN', NULL);

-- Usuario analista (password: analista123)
INSERT INTO users (username, password, role, documento) VALUES 
('analista1', '$2a$10$xpKzX3zZRZMUqZ3Z9V3yZ.D8YqZ3Z9V3yZ.D8YqZ3Z9V3yZ.D8Yq', 'ROLE_ANALISTA', NULL);

-- Afiliados de prueba
INSERT INTO affiliates (documento, nombre, salario, fecha_afiliacion, estado) VALUES 
('1017654311', 'Juan Pérez García', 3000000.00, '2022-01-15', 'ACTIVO'),
('1023456789', 'María González López', 4500000.00, '2021-06-20', 'ACTIVO'),
('1034567890', 'Carlos Rodríguez Martínez', 2500000.00, '2023-03-10', 'ACTIVO'),
('1045678901', 'Ana Martínez Sánchez', 5000000.00, '2020-11-05', 'ACTIVO'),
('1056789012', 'Luis Hernández Gómez', 2000000.00, '2024-01-20', 'INACTIVO');

-- Usuarios afiliados (password: afiliado123)
INSERT INTO users (username, password, role, documento) VALUES 
('juan.perez', '$2a$10$M9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_AFILIADO', '1017654311'),
('maria.gonzalez', '$2a$10$M9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_AFILIADO', '1023456789'),
('carlos.rodriguez', '$2a$10$M9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_AFILIADO', '1034567890');
