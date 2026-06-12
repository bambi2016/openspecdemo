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
    deleted TINYINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_user_role UNIQUE (user_id, role_id, deleted)
);

CREATE TABLE sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_role_permission UNIQUE (role_id, permission_id, deleted)
);

CREATE INDEX idx_sys_user_role_user_id ON sys_user_role (user_id);
CREATE INDEX idx_sys_role_permission_role_id ON sys_role_permission (role_id);

INSERT INTO sys_role (id, role_code, role_name, description, status, deleted)
VALUES (1, 'ADMIN', '管理员', '系统默认管理员角色', 1, 0);

INSERT INTO sys_permission (id, perm_code, perm_name, description, status, deleted)
VALUES
    (1, 'role:create', '新增角色', '允许新增角色', 1, 0),
    (2, 'role:update', '修改角色', '允许修改角色', 1, 0),
    (3, 'role:bind-permission', '绑定角色权限', '允许为角色绑定权限', 1, 0),
    (4, 'permission:list', '查询权限', '允许查询权限列表', 1, 0),
    (5, 'user:bind-role', '绑定用户角色', '允许为用户绑定角色', 1, 0);

INSERT INTO sys_role_permission (role_id, permission_id, status, deleted)
VALUES
    (1, 1, 1, 0),
    (1, 2, 1, 0),
    (1, 3, 1, 0),
    (1, 4, 1, 0),
    (1, 5, 1, 0);
