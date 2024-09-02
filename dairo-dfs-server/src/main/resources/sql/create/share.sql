-- 分享表
CREATE TABLE share
(
    id      INT8 PRIMARY KEY NOT NULL,
    userId  INT8             NOT NULL,-- 所属用户ID
    pwd     VARCHAR(32) NULL, -- 加密分享
    folder  VARCHAR(512)     NOT NULL, -- 分享的文件夹
    names   VARCHAR(3000)    NOT NULL, -- 分享的文件夹或文件名,用|分割
    endDate INT8 NULL, -- 结束日期
    date    DATETIME         NOT NULL -- 创建日期
);
CREATE INDEX index_userId ON share (userId);
