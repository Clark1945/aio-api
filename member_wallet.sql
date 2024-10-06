create database practice;
-- 創建 ENUM 類型
CREATE TYPE member_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');
CREATE TYPE member_role AS ENUM ('USER', 'ADMIN');

-- 創建 members 表
CREATE TABLE member (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    account VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    ip INET,
    wallet_id BIGINT,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status varchar(20) DEFAULT 'ACTIVE',
    role varchar(20) DEFAULT 'USER',
    birthdate DATE,
    profile_picture_url VARCHAR(255),
    last_login TIMESTAMP,
    login_attempts INT DEFAULT 0,
    security_question VARCHAR(255),
    security_answer VARCHAR(255),
    two_factor_enabled BOOLEAN DEFAULT FALSE
);

-- 設置觸發器以在更新時自動更新 updated_at 欄位
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at  = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_timestamp_trigger
BEFORE UPDATE ON member
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE wallet (
    id SERIAL PRIMARY KEY,
    amt NUMERIC(19, 2) NOT NULL CHECK (amt >= 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    last_tx_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_frozen_time TIMESTAMP
);

CREATE TABLE wallet_transaction (
    id SERIAL PRIMARY KEY,
    wallet_id INT NOT NULL REFERENCES wallet(id),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    complete_time TIMESTAMP,
    tx_type VARCHAR(20) NOT NULL CHECK (tx_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER')),
    receiver INT,
    amt NUMERIC(19, 2) NOT NULL CHECK (amt >= 0),
    transaction_status VARCHAR(20),
    transaction_id UUID NOT NULL,
    fee NUMERIC(19, 2) CHECK (fee >= 0),
    description TEXT
);

CREATE UNIQUE INDEX transaction_id_unique ON wallet_transaction (transaction_id);
