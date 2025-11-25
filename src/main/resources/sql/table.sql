-- ----------------------------
-- Table structure for web_album
-- ----------------------------
DROP TABLE IF EXISTS `web_album`;
CREATE TABLE `web_album`
(
    `id`               bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`            varchar(50)       DEFAULT NULL COMMENT '专辑标题',
    `uid`              bigint(20) UNSIGNED NOT NULL COMMENT '用户id',
    `album_cover`      varchar(255)      DEFAULT NULL COMMENT '专辑封面',
    `type`             int               DEFAULT '0' COMMENT '专辑类型',
    `sort`             int               DEFAULT '0' COMMENT '专辑排序',
    `note_count`       bigint(20) UNSIGNED DEFAULT '0' COMMENT '专辑中的笔记数量',
    `collection_count` bigint(20) UNSIGNED DEFAULT '0' COMMENT '收藏数量',
    `create_time`      datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='专辑表';

-- ----------------------------
-- Table structure for web_album_note_relation
-- ----------------------------
DROP TABLE IF EXISTS `web_album_note_relation`;
CREATE TABLE `web_album_note_relation`
(
    `id`          bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `aid`         bigint(20) UNSIGNED NOT NULL COMMENT '专辑id',
    `nid`         bigint(20) UNSIGNED NOT NULL COMMENT '笔记id',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='专辑-笔记关联表';

-- ----------------------------
-- Table structure for web_chat
-- ----------------------------
DROP TABLE IF EXISTS `web_chat`;
CREATE TABLE `web_chat`
(
    `id`          bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `send_uid`    bigint(20) UNSIGNED NOT NULL COMMENT '发送方的用户id',
    `accept_uid`  bigint(20) UNSIGNED NOT NULL COMMENT '接收方的用户id',
    `content`     longtext COMMENT '聊天内容',
    `msg_type`    int               DEFAULT '0' COMMENT '消息类型（0通知消息，1是文本消息，2是图片消息，3是语音消息，4是视频消息，5 自定义消息）',
    `chat_type`   int               DEFAULT '0' COMMENT '聊天类型（0是私聊，1是群聊）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天表';

-- ----------------------------
-- Table structure for web_chat_user_relation
-- ----------------------------
DROP TABLE IF EXISTS `web_chat_user_relation`;
CREATE TABLE `web_chat_user_relation`
(
    `id`          bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `send_uid`    bigint(20) UNSIGNED NOT NULL COMMENT '发送方的用户id',
    `accept_uid`  bigint(20) UNSIGNED NOT NULL COMMENT '接收方的用户id',
    `content`     longtext COMMENT '聊天内容',
    `count`       int               DEFAULT '0' COMMENT '未读消息的数量',
    `chat_type`   int               DEFAULT '0' COMMENT '聊天类型（0是私聊，1是群聊）',
    `msg_type`    int               DEFAULT '0' COMMENT '消息类型（0通知消息，1是文本消息，2是图片消息，3是语音消息，4是视频消息，5 自定义消息）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天-用户关联表';

-- ----------------------------
-- Table structure for web_comment
-- ----------------------------
DROP TABLE IF EXISTS `web_comment`;
CREATE TABLE `web_comment`
(
    `id`                bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `nid`               bigint(20) UNSIGNED DEFAULT NULL COMMENT '笔记id',
    `uid`               bigint(20) UNSIGNED DEFAULT NULL COMMENT '发布评论的用户id',
    `pid`               bigint(20) UNSIGNED DEFAULT '0' COMMENT '根评论id',
    `reply_id`          bigint(20) UNSIGNED DEFAULT '0' COMMENT '回复的评论id',
    `level`             int               DEFAULT '0' COMMENT '评论的等级',
    `content`           longtext COMMENT '评论的内容',
    `like_count`        bigint(20) UNSIGNED DEFAULT '0' COMMENT '点赞数量',
    `two_comment_count` bigint(20) UNSIGNED DEFAULT '0' COMMENT '二级评论数量',
    `create_time`       datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';

-- ----------------------------
-- Table structure for web_comment_sync
-- ----------------------------
DROP TABLE IF EXISTS `web_comment_sync`;
CREATE TABLE `web_comment_sync`
(
    `id`                bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `nid`               bigint(20) UNSIGNED DEFAULT NULL COMMENT '笔记id',
    `uid`               bigint(20) UNSIGNED DEFAULT NULL COMMENT '发布评论的用户id',
    `pid`               bigint(20) UNSIGNED DEFAULT '0' COMMENT '根评论id',
    `reply_id`          bigint(20) UNSIGNED DEFAULT '0' COMMENT '回复的评论id',
    `level`             int               DEFAULT '0' COMMENT '评论的等级',
    `content`           longtext COMMENT '评论的内容',
    `like_count`        bigint(20) UNSIGNED DEFAULT '0' COMMENT '点赞数量',
    `two_comment_count` bigint(20) UNSIGNED DEFAULT '0' COMMENT '二级评论数量',
    `create_time`       datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='同步评论表';

-- ----------------------------
-- Table structure for web_follow
-- ----------------------------
DROP TABLE IF EXISTS `web_follow`;
CREATE TABLE `web_follow`
(
    `id`          bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `uid`         bigint(20) UNSIGNED NOT NULL COMMENT '关注者id',
    `fid`         bigint(20) UNSIGNED NOT NULL COMMENT '被关注者id',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='关注表';

-- ----------------------------
-- Table structure for web_like_or_collect
-- ----------------------------
DROP TABLE IF EXISTS `web_like_or_collect`;
CREATE TABLE `web_like_or_collect`
(
    `id`                    bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `uid`                   bigint(20) UNSIGNED NOT NULL COMMENT '点赞的用户',
    `like_or_collection_id` bigint(20) UNSIGNED NOT NULL COMMENT '点赞或收藏的对象id(可能是图片或者评论)',
    `publish_uid`           bigint(20) UNSIGNED NOT NULL COMMENT '点赞和收藏通知的用户',
    `type`                  int      NOT NULL COMMENT '点赞收藏类型:1 点赞图片 2点赞评论  3收藏图片 4收藏专辑',
    `create_time`           datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`           datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=357 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点赞或收藏表';

-- ----------------------------
-- Table structure for web_login_log
-- ----------------------------
DROP TABLE IF EXISTS `web_login_log`;
CREATE TABLE `web_login_log`
(
    `id`             bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `uid`            bigint(20) UNSIGNED NOT NULL COMMENT '用户ID',
    `ipaddr`         varchar(128)      DEFAULT '' COMMENT '登录IP地址',
    `login_location` varchar(255)      DEFAULT '' COMMENT '登录地点',
    `browser`        varchar(50)       DEFAULT '' COMMENT '浏览器类型',
    `os`             varchar(50)       DEFAULT '' COMMENT '操作系统',
    `status`         int               DEFAULT 1 COMMENT '登录状态（0失败 1成功）',
    `msg`            varchar(255)      DEFAULT '' COMMENT '提示消息',
    `create_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=153 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录信息表';

-- ----------------------------
-- Table structure for web_navbar
-- ----------------------------
DROP TABLE IF EXISTS `web_navbar`;
CREATE TABLE `web_navbar`
(
    `id`           bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`        varchar(50)       DEFAULT NULL COMMENT '分类标题',
    `pid`          bigint(20) UNSIGNED DEFAULT NULL COMMENT '分类父id',
    `sort`         int               DEFAULT '0' COMMENT '分类排序',
    `like_count`   bigint            DEFAULT '0' COMMENT '喜欢数量',
    `description`  longtext COMMENT '分类描述',
    `normal_cover` varchar(255)      DEFAULT NULL COMMENT '分类的封面，如果是一级分类就是随便看看的封面，二级分类则是主封面',
    `hot_cover`    varchar(255)      DEFAULT NULL COMMENT '热门封面',
    `create_time`  datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分类表';

-- ----------------------------
-- Table structure for web_note
-- ----------------------------
DROP TABLE IF EXISTS `web_note`;
CREATE TABLE `web_note`
(
    `id`                bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`             varchar(50)       DEFAULT NULL COMMENT '笔记标题',
    `content`           longtext COMMENT '笔记内容',
    `note_cover`        varchar(255)      DEFAULT NULL COMMENT '笔记封面',
    `note_cover_height` int               DEFAULT NULL COMMENT '笔记高度',
    `uid`               bigint(20) UNSIGNED NOT NULL COMMENT '用户id',
    `cid`               bigint(20) UNSIGNED DEFAULT NULL COMMENT '笔记所属二级分类id',
    `cpid`              bigint(20) UNSIGNED DEFAULT NULL COMMENT '笔记所属一级分类id',
    `urls`              longtext COMMENT '图片urls',
    `count`             int               DEFAULT NULL COMMENT '图片数量',
    `pinned`            int               DEFAULT '0' COMMENT '是否置顶',
    `audit_status`      int               DEFAULT NULL COMMENT '审核状态',
    `note_type`         int               DEFAULT '0' COMMENT '笔记类型（0：图文，1:视频）',
    `view_count`        bigint            DEFAULT '0' COMMENT '笔记浏览次数',
    `like_count`        bigint            DEFAULT '0' COMMENT '笔记点赞次数',
    `collection_count`  bigint            DEFAULT '0' COMMENT '笔记收藏次数',
    `comment_count`     bigint            DEFAULT '0' COMMENT '笔记评论次数',
    `create_time`       datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY                 `update_date_index` (`update_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=446 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='笔记表';

-- ----------------------------
-- Table structure for web_tag
-- ----------------------------
DROP TABLE IF EXISTS `web_tag`;
CREATE TABLE `web_tag`
(
    `id`          bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`       varchar(50)       DEFAULT NULL COMMENT '标签标题',
    `like_count`  bigint            DEFAULT NULL COMMENT '标签使用次数',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';

-- ----------------------------
-- Table structure for web_tag_note_relation
-- ----------------------------
DROP TABLE IF EXISTS `web_tag_note_relation`;
CREATE TABLE `web_tag_note_relation`
(
    `id`          bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `nid`         bigint(20) UNSIGNED NOT NULL COMMENT '笔记id',
    `tid`         bigint(20) UNSIGNED NOT NULL COMMENT '标签id',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签-笔记关联表';

-- ----------------------------
-- Table structure for web_user
-- ----------------------------
DROP TABLE IF EXISTS `web_user`;
CREATE TABLE `web_user`
(
    `id`             bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `hs_id`          bigint(20) UNSIGNED NOT NULL COMMENT '账户id',
    `username`       varchar(50) NOT NULL COMMENT '用户名',
    `password`       varchar(100)         DEFAULT NULL COMMENT '密码',
    `avatar`         varchar(225)         DEFAULT NULL COMMENT '头像',
    `gender`         tinyint              DEFAULT '0' COMMENT '性别',
    `phone`          varchar(50)          DEFAULT NULL COMMENT '手机号',
    `email`          varchar(100)         DEFAULT NULL COMMENT 'email',
    `tags`           varchar(255)         DEFAULT NULL COMMENT '用户标签',
    `description`    longtext COMMENT '描述',
    `status`         tinyint              DEFAULT '0' COMMENT '用户状态（0：正常，1：异常）',
    `user_cover`     varchar(255)         DEFAULT NULL COMMENT '用户封面',
    `birthday`       varchar(50)          DEFAULT NULL COMMENT '生日',
    `address`        varchar(50)          DEFAULT NULL COMMENT '地址',
    `trend_count`    bigint(20) UNSIGNED DEFAULT '0' COMMENT '笔记数量',
    `follower_count` bigint(20) UNSIGNED DEFAULT '0' COMMENT '关注数量',
    `fan_count`      bigint(20) UNSIGNED DEFAULT '0' COMMENT '粉丝数量',
    `login_ip`       varchar(128)         DEFAULT '' COMMENT '最后登录IP',
    `login_date`     datetime             DEFAULT NULL COMMENT '最后登录时间',
    `is_online`      tinyint              DEFAULT '0' COMMENT '是否在线',
    `remark`         varchar(255)         DEFAULT NULL COMMENT '备注',
    `deleted`        int                  DEFAULT '0' COMMENT '是否注销（0：未注销，1：注销）',
    `create_time`    datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ----------------------------
-- Table structure for web_user_note_relation
-- ----------------------------
DROP TABLE IF EXISTS `web_user_note_relation`;
CREATE TABLE `web_user_note_relation`
(
    `id`          bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `nid`         bigint(20) UNSIGNED NOT NULL COMMENT '笔记id',
    `uid`         bigint(20) UNSIGNED NOT NULL COMMENT '用户id',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=327 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户-笔记关系表';