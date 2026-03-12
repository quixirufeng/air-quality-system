
CREATE DATABASE IF NOT EXISTS air_quality_db;
USE air_quality_db;

DROP TABLE IF EXISTS air_record;
CREATE TABLE air_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city_name VARCHAR(50) NOT NULL COMMENT '城市',
    aqi INT NOT NULL COMMENT 'AQI指数',
    pm25 DOUBLE COMMENT 'PM2.5',
    level VARCHAR(20) COMMENT '污染等级',
    record_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '监测时间'
);

INSERT INTO air_record (city_name, aqi, pm25, level, record_time) VALUES 
('北京', 85, 42.5, '良', NOW()),
('上海', 55, 20.1, '良', NOW()),
('成都', 120, 80.5, '轻度污染', NOW());
