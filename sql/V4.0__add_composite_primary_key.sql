ALTER TABLE `files`
CHANGE COLUMN `part_file_name` `part_file_name` VARCHAR(255) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NOT NULL ,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`, `part_file_name`);
