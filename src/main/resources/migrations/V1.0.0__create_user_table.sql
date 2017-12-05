CREATE TABLE `platform_user` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `nickname` varchar(64) NOT NULL COMMENT '昵称',
  `target_type` varchar(32) NOT NULL COMMENT '平台类型',
  `target_id` int(10) UNSIGNED NOT NULL COMMENT '子表id',
  `created_time` TIMESTAMP NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `client_user` ( 
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `nickname` varchar(64) NOT NULL COMMENT '昵称',
  `email` varchar(64) COMMENT '邮箱',
  `mobile` varchar(64) COMMENT '手机号码',
  `client_user_id` varchar(64) NOT NULL COMMENT '网校上的用户id',
  `platform_id` int(10) UNSIGNED NOT NULL COMMENT 'platform_user的id',
  `client_key` varchar(64) NOT NULL COMMENT '网校对应的key',
  `created_time` TIMESTAMP NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间', 
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `wechat_user` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `nickname` varchar(64) NOT NULL COMMENT '昵称',
  `openid` varchar(128) NOT NULL COMMENT 'openid',
  `sex` tinyint(1) NOT NULL COMMENT 'openid',
  `province` varchar(128),
  `city` varchar(128),
  `country` varchar(128),
  `headimgurl` varchar(256),
  `privilege` TEXT,
  `unionid` varchar(128),
  `created_time` TIMESTAMP NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
