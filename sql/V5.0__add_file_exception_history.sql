DROP TABLE IF EXISTS `files_exception_history`;
CREATE TABLE `files_exception_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `exception_message` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `general_file_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `part_file_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `transaction_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci