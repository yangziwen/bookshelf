CREATE TABLE `book` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `bookId` INT,
  `name` VARCHAR(200) DEFAULT NULL,
  `publisher` VARCHAR(40) DEFAULT NULL,
  `isbn` VARCHAR(60) DEFAULT NULL,
  `year` VARCHAR(4) DEFAULT NULL,
  `pages` INT DEFAULT NULL,
  `language` VARCHAR(20) DEFAULT NULL,
  `size` VARCHAR(10) DEFAULT NULL,
  `format` VARCHAR(10) DEFAULT NULL,
  `downloadUrl` VARCHAR(200) DEFAULT NULL,
  `coverImgUrl` VARCHAR(200) DEFAULT NULL,
  `storagePath` VARCHAR(200) DEFAULT NULL,
  `authorName` VARCHAR(300) DEFAULT NULL,
  `pageUrl` VARCHAR(200) DEFAULT NULL
);

CREATE TABLE `cron_job` (
	`id` INT PRIMARY KEY AUTO_INCREMENT,
	`name` VARCHAR(20) DEFAULT NULL,
	`type` VARCHAR(20) NOT NULL UNIQUE,
	`cron` VARCHAR(50) DEFAULT NULL,
	`update_time` DATETIME DEFAULT NULL,
	`enabled` TINYINT DEFAULT 0
);