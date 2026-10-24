-- Пароль "admin123" закодирован BCrypt
INSERT INTO users (id, username, password, role) VALUES
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye1VSRXmrNjKkR/HSJG9j.PjXfK9I5HAq', 'ROLE_ADMIN');

-- Пароль "user123" закодирован BCrypt
INSERT INTO users (id, username, password, role) VALUES
(2, 'user', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ROLE_USER');