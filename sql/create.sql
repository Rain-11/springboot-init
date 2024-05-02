create table ApexLink.user
(
    id            bigint auto_increment comment 'id'
        primary key,
    user_password varchar(512)                not null comment '密码',
    user_name     varchar(256)                null comment '用户昵称',
    user_avatar   varchar(1024)               null comment '用户头像',
    user_profile  varchar(512)                null comment '用户简介',
    user_role     varchar(256) default 'user' not null comment '用户角色：user/admin/ban',
    create_time   datetime                    null comment '创建时间',
    update_time   datetime                    null comment '更新时间',
    is_delete     tinyint      default 0      not null comment '是否删除',
    email         varchar(255)                not null comment '登录邮箱',
    secret_id     varchar(255)                null comment '签名id',
    secret_key    varchar(255)                null comment '签名秘钥',
    wallet        int          default 0      null comment '钱包'
)
    comment '用户' collate = utf8mb4_unicode_ci
                   row_format = DYNAMIC;

