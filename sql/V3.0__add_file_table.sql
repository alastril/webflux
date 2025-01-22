DROP TABLE IF EXISTS `files`;
CREATE TABLE `files` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file` mediumblob,
  `part_file_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `general_file_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci