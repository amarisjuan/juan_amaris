-- Script básico para crear tablas de usuarios/roles usadas por Spring Security

CREATE TABLE IF NOT EXISTS roles (
  id BIGINT PRIMARY KEY,
  nombre VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS usuarios (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(128) NOT NULL,
  activo BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS usuarios_roles (
  usuario_id BIGINT NOT NULL,
  rol_id BIGINT NOT NULL,
  PRIMARY KEY (usuario_id, rol_id),
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  FOREIGN KEY (rol_id) REFERENCES roles(id)
);

-- Datos iniciales (también se crean al iniciar la app mediante DataInitializer)
INSERT INTO roles (id, nombre) VALUES (1, 'ADMIN') ON DUPLICATE KEY UPDATE nombre=nombre;
INSERT INTO roles (id, nombre) VALUES (2, 'USER') ON DUPLICATE KEY UPDATE nombre=nombre;

-- Passwords en este script deben ir encriptados; el DataInitializer usa BCrypt.
-- Para pruebas puede dejarse vacío y usar el CommandLineRunner que crea usuarios 'admin' y 'user'.
