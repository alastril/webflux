ALTER TABLE `files`
ADD COLUMN `transaction_id` VARCHAR(100) NULL AFTER `general_file_name`;
