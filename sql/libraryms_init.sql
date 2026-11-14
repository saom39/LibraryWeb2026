-- ============================================================
-- 图书借阅管理平台 数据库初始化脚本
-- 数据库：libraryms  （MySQL 8.x，字符集 utf8mb4）
-- 说明：本脚本会先删除同名数据库再重建，并写入演示数据。
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
DROP DATABASE IF EXISTS `libraryms`;
CREATE DATABASE `libraryms` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `libraryms`;

-- ------------------------------------------------------------
-- 用户表（单表继承：管理员 / 操作员 / 教师 / 学生 共用一张表）
-- ------------------------------------------------------------
CREATE TABLE `user` (
  `id`              char(36)     NOT NULL,
  `createTime`      datetime     DEFAULT NULL,
  `updateTime`      datetime     DEFAULT NULL,
  `name`            varchar(50)  DEFAULT NULL,
  `password`        varchar(100) DEFAULT NULL,
  `phone`           varchar(20)  DEFAULT NULL,
  `role`            varchar(20)  DEFAULT NULL,
  `major`           varchar(50)  DEFAULT NULL,
  `loginTime`       datetime     DEFAULT NULL,
  `permissionLevel` int          DEFAULT NULL,
  `entryTime`       date         DEFAULT NULL,
  `duty`            varchar(50)  DEFAULT NULL,
  `area`            varchar(50)  DEFAULT NULL,
  `grade`           varchar(20)  DEFAULT NULL,
  `college`         varchar(50)  DEFAULT NULL,
  `title`           varchar(50)  DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

INSERT INTO `user` (`id`, `createTime`, `updateTime`, `name`, `password`, `phone`, `role`, `major`, `loginTime`, `permissionLevel`, `entryTime`, `duty`, `area`, `grade`, `college`, `title`) VALUES
('10000000-0000-0000-0000-000000000001', '2026-09-01 09:00:00', '2026-09-01 09:00:00', 'admin',    'admin123',    '13800000001', '管理员', '计算机科学与技术', NULL, 1, '2020-09-01', NULL, NULL, NULL, NULL, NULL),
('10000000-0000-0000-0000-000000000002', '2026-09-01 09:00:00', '2026-09-01 09:00:00', 'operator', 'operator123', '13800000002', '操作员', '软件工程',         NULL, NULL, NULL, '图书管理员', '图书馆一楼', NULL, NULL, NULL),
('10000000-0000-0000-0000-000000000003', '2026-09-01 09:00:00', '2026-09-01 09:00:00', 'student1', 'student123',  '13800000004', '学生',   '计算机科学与技术', NULL, NULL, NULL, NULL, NULL, '2022级', '计算机学院', NULL),
('10000000-0000-0000-0000-000000000004', '2026-09-01 09:00:00', '2026-09-01 09:00:00', 'teacher',  'teacher123',  '13800000003', '教师',   '计算机科学',       NULL, NULL, NULL, NULL, NULL, NULL, '计算机学院', '教授');

-- ------------------------------------------------------------
-- 图书表
-- ------------------------------------------------------------
CREATE TABLE `book` (
  `id`           char(36)      NOT NULL,
  `createTime`   datetime      DEFAULT NULL,
  `updateTime`   datetime      DEFAULT NULL,
  `title`        varchar(60)   DEFAULT NULL,
  `author`       varchar(60)   DEFAULT NULL,
  `type`         varchar(20)   DEFAULT NULL,
  `publisher`    varchar(60)   DEFAULT NULL,
  `days`         int           DEFAULT NULL,
  `price`        double        DEFAULT NULL,
  `stock`        int           DEFAULT NULL,
  `status`       varchar(10)   DEFAULT NULL,
  `introduction` varchar(512)  DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

INSERT INTO `book` (`id`, `createTime`, `updateTime`, `title`, `author`, `type`, `publisher`, `days`, `price`, `stock`, `status`, `introduction`) VALUES
('20000000-0000-0000-0000-000000000001', '2026-09-02 10:00:00', '2026-09-02 10:00:00', 'Java核心技术 卷I', '凯·霍斯特曼', '计算机', '机械工业出版社', 7, 119.0, 3, '正常', 'Java 经典入门教材，覆盖 Java 基础语法、面向对象与集合框架。'),
('20000000-0000-0000-0000-000000000002', '2026-09-02 10:10:00', '2026-09-02 10:10:00', '数据结构与算法', '王红梅', '计算机', '清华大学出版社', 14, 49.5, 2, '正常', '系统讲解线性表、树、图等数据结构及常用算法设计。'),
('20000000-0000-0000-0000-000000000003', '2026-09-03 10:00:00', '2026-09-03 10:00:00', '计算机网络：自顶向下方法', '库罗斯', '计算机', '机械工业出版社', 14, 89.0, 2, '正常', '从应用层出发自顶向下讲解计算机网络各层协议。'),
('20000000-0000-0000-0000-000000000004', '2026-09-04 10:00:00', '2026-09-04 10:00:00', '平凡的世界', '路遥', '文学', '北京十月文艺出版社', 30, 108.0, 3, '正常', '全景式展现中国当代城乡社会生活的长篇小说。'),
('20000000-0000-0000-0000-000000000005', '2026-09-04 10:20:00', '2026-09-04 10:20:00', '活着', '余华', '文学', '作家出版社', 30, 28.0, 2, '正常', '讲述一个人在时代洪流中历经苦难却依然活着的故事。'),
('20000000-0000-0000-0000-000000000006', '2026-09-05 10:00:00', '2026-09-05 10:00:00', '明朝那些事儿', '当年明月', '历史', '中国海关出版社', 30, 358.0, 2, '正常', '以轻松笔法讲述明朝三百年间的历史故事。'),
('20000000-0000-0000-0000-000000000007', '2026-09-06 10:00:00', '2026-09-06 10:00:00', '人工智能导论', '王万良', '计算机', '高等教育出版社', 14, 45.0, 2, '正常', '人工智能基础理论与典型应用入门教材。'),
('20000000-0000-0000-0000-000000000008', '2026-09-06 10:30:00', '2026-09-06 10:30:00', '经济学原理', '曼昆', '经济', '北京大学出版社', 30, 88.0, 2, '正常', '微观与宏观经济学入门经典读物。');

-- ------------------------------------------------------------
-- 图书副本表（一本图书对应多条副本，副本可独立借阅）
-- ------------------------------------------------------------
CREATE TABLE `bookcopy` (
  `id`         char(36) NOT NULL,
  `createTime` datetime DEFAULT NULL,
  `updateTime` datetime DEFAULT NULL,
  `bookId`     char(36) DEFAULT NULL,
  `isBorrowed` bit(1)   DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `bookId` (`bookId`),
  CONSTRAINT `fk_bookcopy_book` FOREIGN KEY (`bookId`) REFERENCES `book` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

INSERT INTO `bookcopy` (`id`, `createTime`, `updateTime`, `bookId`, `isBorrowed`) VALUES
('30000000-0000-0000-0000-000000000001', '2026-09-02 10:00:00', '2026-09-02 10:00:00', '20000000-0000-0000-0000-000000000001', b'0'),
('30000000-0000-0000-0000-000000000002', '2026-09-02 10:00:00', '2026-09-02 10:00:00', '20000000-0000-0000-0000-000000000001', b'0'),
('30000000-0000-0000-0000-000000000003', '2026-09-02 10:00:00', '2026-09-02 10:00:00', '20000000-0000-0000-0000-000000000001', b'0'),
('30000000-0000-0000-0000-000000000004', '2026-09-02 10:10:00', '2026-09-02 10:10:00', '20000000-0000-0000-0000-000000000002', b'1'),
('30000000-0000-0000-0000-000000000005', '2026-09-02 10:10:00', '2026-09-02 10:10:00', '20000000-0000-0000-0000-000000000002', b'0'),
('30000000-0000-0000-0000-000000000006', '2026-09-03 10:00:00', '2026-09-03 10:00:00', '20000000-0000-0000-0000-000000000003', b'0'),
('30000000-0000-0000-0000-000000000007', '2026-09-03 10:00:00', '2026-09-03 10:00:00', '20000000-0000-0000-0000-000000000003', b'0'),
('30000000-0000-0000-0000-000000000008', '2026-09-04 10:00:00', '2026-09-04 10:00:00', '20000000-0000-0000-0000-000000000004', b'1'),
('30000000-0000-0000-0000-000000000009', '2026-09-04 10:00:00', '2026-09-04 10:00:00', '20000000-0000-0000-0000-000000000004', b'0'),
('30000000-0000-0000-0000-000000000010', '2026-09-04 10:00:00', '2026-09-04 10:00:00', '20000000-0000-0000-0000-000000000004', b'0'),
('30000000-0000-0000-0000-000000000011', '2026-09-04 10:20:00', '2026-09-04 10:20:00', '20000000-0000-0000-0000-000000000005', b'0'),
('30000000-0000-0000-0000-000000000012', '2026-09-04 10:20:00', '2026-09-04 10:20:00', '20000000-0000-0000-0000-000000000005', b'0'),
('30000000-0000-0000-0000-000000000013', '2026-09-05 10:00:00', '2026-09-05 10:00:00', '20000000-0000-0000-0000-000000000006', b'0'),
('30000000-0000-0000-0000-000000000014', '2026-09-05 10:00:00', '2026-09-05 10:00:00', '20000000-0000-0000-0000-000000000006', b'0'),
('30000000-0000-0000-0000-000000000015', '2026-09-06 10:00:00', '2026-09-06 10:00:00', '20000000-0000-0000-0000-000000000007', b'0'),
('30000000-0000-0000-0000-000000000016', '2026-09-06 10:00:00', '2026-09-06 10:00:00', '20000000-0000-0000-0000-000000000007', b'0'),
('30000000-0000-0000-0000-000000000017', '2026-09-06 10:30:00', '2026-09-06 10:30:00', '20000000-0000-0000-0000-000000000008', b'0'),
('30000000-0000-0000-0000-000000000018', '2026-09-06 10:30:00', '2026-09-06 10:30:00', '20000000-0000-0000-0000-000000000008', b'0');

-- ------------------------------------------------------------
-- 借阅记录表
-- ------------------------------------------------------------
CREATE TABLE `loan` (
  `id`         char(36)     NOT NULL,
  `createTime` datetime     DEFAULT NULL,
  `updateTime` datetime     DEFAULT NULL,
  `bookCopyId` char(36)     DEFAULT NULL,
  `userId`     char(36)     DEFAULT NULL,
  `borrowDate` datetime     DEFAULT NULL,
  `returnDate` datetime     DEFAULT NULL,
  `dueDate`    datetime     DEFAULT NULL,
  `status`     varchar(10)  DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `bookCopyId` (`bookCopyId`),
  KEY `userId` (`userId`),
  CONSTRAINT `fk_loan_bookcopy` FOREIGN KEY (`bookCopyId`) REFERENCES `bookcopy` (`id`),
  CONSTRAINT `fk_loan_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

INSERT INTO `loan` (`id`, `createTime`, `updateTime`, `bookCopyId`, `userId`, `borrowDate`, `returnDate`, `dueDate`, `status`) VALUES
('40000000-0000-0000-0000-000000000001', '2026-10-01 10:00:00', '2026-10-05 15:00:00', '30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003', '2026-10-01 10:00:00', '2026-10-05 15:00:00', '2026-10-08 10:00:00', '已还'),
('40000000-0000-0000-0000-000000000002', '2026-10-20 09:00:00', '2026-10-20 09:00:00', '30000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000003', '2026-10-20 09:00:00', NULL, '2026-11-03 09:00:00', '未还'),
('40000000-0000-0000-0000-000000000003', '2026-11-10 10:00:00', '2026-11-10 10:00:00', '30000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000004', '2026-11-10 10:00:00', NULL, '2026-12-10 10:00:00', '未还');

-- ------------------------------------------------------------
-- 图书推荐表（教师提交，管理员审阅）
-- ------------------------------------------------------------
CREATE TABLE `bookrecommend` (
  `id`         char(36)     NOT NULL,
  `createTime` datetime     DEFAULT NULL,
  `updateTime` datetime     DEFAULT NULL,
  `userId`     char(36)     DEFAULT NULL,
  `bookName`   varchar(60)  DEFAULT NULL,
  `author`     varchar(60)  DEFAULT NULL,
  `publisher`  varchar(60)  DEFAULT NULL,
  `price`      double       DEFAULT NULL,
  `status`     varchar(10)  DEFAULT NULL,
  `reason`     varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`),
  CONSTRAINT `fk_recommend_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

INSERT INTO `bookrecommend` (`id`, `createTime`, `updateTime`, `userId`, `bookName`, `author`, `publisher`, `price`, `status`, `reason`) VALUES
('50000000-0000-0000-0000-000000000001', '2026-11-12 14:00:00', '2026-11-12 14:00:00', '10000000-0000-0000-0000-000000000004', '深入理解计算机系统', '兰德尔·布莱恩特', '机械工业出版社', 139.0, '待审阅', NULL);

SET FOREIGN_KEY_CHECKS = 1;
