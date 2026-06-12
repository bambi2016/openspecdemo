DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(128) NOT NULL,
    nickname VARCHAR(64),
    phone VARCHAR(32),
    email VARCHAR(128),
    status TINYINT NOT NULL DEFAULT 1,
    last_login_time TIMESTAMP NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_user_username UNIQUE (username)
);

CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(64) NOT NULL,
    description VARCHAR(255),
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_role_code UNIQUE (role_code)
);

CREATE TABLE sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    perm_code VARCHAR(128) NOT NULL,
    perm_name VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_permission_code UNIQUE (perm_code)
);

CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

INSERT INTO sys_user (id, username, password, nickname, phone, email, status, deleted)
VALUES
    (1, 'admin', '$2a$10$hi', 'Admin', '13800000000', 'admin@example.com', 1, 0),
    (2, 'disabled', '$2a$10$hi', 'Disabled', '13900000000', 'disabled@example.com', 0, 0),
    (3, 'deleted', '$2a$10$hi', 'Deleted', '13700000000', 'deleted@example.com', 1, 1);

INSERT INTO sys_role (id, role_code, role_name, description, status, deleted)
VALUES
    (1, 'ADMIN', '管理员', 'admin role', 1, 0),
    (2, 'DISABLED_ROLE', '禁用角色', 'disabled role', 0, 0);

INSERT INTO sys_permission (id, perm_code, perm_name, description, status, deleted)
VALUES
    (1, 'role:create', '新增角色', 'create role', 1, 0),
    (2, 'permission:list', '查询权限', 'list permission', 1, 0),
    (3, 'role:disabled', '禁用权限', 'disabled permission', 0, 0);

INSERT INTO sys_user_role (user_id, role_id, status, deleted)
VALUES
    (1, 1, 1, 0),
    (1, 2, 1, 0);

INSERT INTO sys_role_permission (role_id, permission_id, status, deleted)
VALUES
    (1, 1, 1, 0),
    (1, 2, 1, 0),
    (2, 3, 1, 0);

ALTER TABLE sys_user ALTER COLUMN id RESTART WITH 100;
ALTER TABLE sys_role ALTER COLUMN id RESTART WITH 100;
ALTER TABLE sys_permission ALTER COLUMN id RESTART WITH 100;
