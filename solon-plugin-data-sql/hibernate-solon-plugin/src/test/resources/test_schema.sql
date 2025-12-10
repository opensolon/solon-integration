-- ============================================
-- 测试数据库表结构SQL脚本
-- ============================================
-- 
-- 说明：
--   1. 此脚本包含测试所需的所有表结构
--   2. 适用于MySQL数据库
--   3. 如果使用其他数据库，请根据实际情况调整
--   4. 也可以使用 hbm2ddl.auto=create 自动创建表
--
-- 使用方法：
--   mysql -u root -p test < test_schema.sql
--   或者在数据库客户端中执行此脚本
--
-- ============================================

-- 用户表
CREATE TABLE IF NOT EXISTS `test_user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '用户名',
    `age` INT(11) NOT NULL COMMENT '年龄',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表（测试用）';

-- 产品表
CREATE TABLE IF NOT EXISTS `product` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code` VARCHAR(32) NOT NULL COMMENT '产品编码',
    `name` VARCHAR(200) NOT NULL COMMENT '产品名称',
    `description` TEXT COMMENT '产品描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `stock` INT(11) NOT NULL DEFAULT '0' COMMENT '库存',
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PUBLISHED/ARCHIVED',
    `category_id` BIGINT(20) NOT NULL COMMENT '分类ID',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_code` (`code`),
    KEY `idx_product_name` (`name`),
    KEY `idx_product_category` (`category_id`),
    KEY `idx_product_status` (`status`),
    KEY `idx_product_category_status` (`category_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表（测试用）';

-- 分类表
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT(20) DEFAULT NULL COMMENT '父分类ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_name_parent` (`name`, `parent_id`),
    KEY `fk_category_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表（测试用）';

-- ============================================
-- 清理脚本（可选）
-- ============================================
-- 如果需要重新创建表，可以先执行以下语句：
-- 
-- DROP TABLE IF EXISTS `test_user`;
-- DROP TABLE IF EXISTS `product`;
-- DROP TABLE IF EXISTS `category`;
--
-- ============================================

