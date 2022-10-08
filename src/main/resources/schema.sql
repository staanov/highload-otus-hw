USE otushw;

CREATE TABLE IF NOT EXISTS user (
    user_id BIGINT(255) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    login VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    age INT(100) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    city VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS interest (
    user_id BIGINT(255) NOT NULL REFERENCES user(user_id),
    interest VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, interest)
);

CREATE TABLE IF NOT EXISTS security (
    user_id BIGINT(255) NOT NULL PRIMARY KEY REFERENCES user(user_id),
    login VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    enabled TINYINT NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS authority (
    login VARCHAR(50) NOT NULL REFERENCES security(login),
    authority VARCHAR(50) NOT NULL,
    PRIMARY KEY (login, authority)
);

CREATE TABLE IF NOT EXISTS friend (
    first_user_id BIGINT(255) NOT NULL,
    second_user_id BIGINT(255) NOT NULL,
    PRIMARY KEY (first_user_id, second_user_id)
);
