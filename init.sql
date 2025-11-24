-- init.sql

USE sprout_db;

-- 1. 기존 테이블 삭제
DROP TABLE IF EXISTS users;

-- 2. 테이블 생성
CREATE TABLE `users` (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        name VARCHAR(255),
                        age INT
);

-- 3. Mock Data (더미 데이터) 삽입
INSERT INTO `users` (name, age) VALUES ('Alice', 25);
INSERT INTO `users` (name, age) VALUES ('Bob', 30);
INSERT INTO `users` (name, age) VALUES ('Charlie', 22);
INSERT INTO `users` (name, age) VALUES ('David', 28);
INSERT INTO `users` (name, age) VALUES ('Eve', 35);