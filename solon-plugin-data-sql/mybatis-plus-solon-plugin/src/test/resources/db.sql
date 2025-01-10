
CREATE TABLE `users` (
                        `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
                        `uuid` varchar(40) DEFAULT NULL COMMENT 'KEY',
                        PRIMARY KEY (`id`)
);

CREATE TABLE `test` (
                        `id` int NOT NULL AUTO_INCREMENT,
                        `v1` int DEFAULT NULL,
                        `v2` int DEFAULT NULL,
                        PRIMARY KEY (`id`)
);