CREATE DATABASE IF NOT EXISTS `test` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `test`;

DROP TABLE IF EXISTS `project`;

CREATE TABLE `project` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `owner` VARCHAR(50) NULL,
  `status` INT NOT NULL,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NOT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `external_call_log`;

CREATE TABLE `external_call_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `trace_id` VARCHAR(64) NOT NULL,
  `request_id` VARCHAR(64) NOT NULL,
  `service` VARCHAR(32) NOT NULL,
  `target_url` VARCHAR(512) NOT NULL,
  `http_method` VARCHAR(16) NOT NULL,
  `query_string` VARCHAR(512) NULL,
  `http_status` INT NULL,
  `success` TINYINT NOT NULL,
  `attempt` INT NOT NULL,
  `duration_ms` BIGINT NULL,
  `exception_type` VARCHAR(128) NULL,
  `exception_message` VARCHAR(1024) NULL,
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_trace_id` (`trace_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

