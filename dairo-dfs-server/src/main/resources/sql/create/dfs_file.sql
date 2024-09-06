-- 文件表
CREATE TABLE dfs_file
(
    id           INT8 PRIMARY KEY NOT NULL,           -- 主键
    userId       INT8             NOT NULL,-- 所属用户ID
    parentId     INT8             NOT NULL DEFAULT 0, -- 父目录ID,当isExtra=1时，则标识所属文件的id
    name         VARCHAR(256)     NOT NULL,           -- 名称
    size         INT8             NOT NULL,           -- 大小
    contentType  VARCHAR(32) NULL,                    -- 文件类型(文件专用)
    localId      INT8             NOT NULL DEFAULT 0, -- 本地文件存储id(文件专用)
    date         DATETIME         NOT NULL,           -- 创建日期
    property     TEXT NULL,                           -- 文件属性，比如图片尺寸，视频分辨率等信息，JSON字符串
    isExtra      INT1             NOT NULL DEFAULT 0, -- 是否附属文件，比如视频的标清文件，高清文件，PSD图片的预览图片，cr3的预览图片等
    isHistory    INT1             NOT NULL DEFAULT 0, -- 是否历史版本(文件专用),1:历史版本 0:当前版本
    deleteDate   INT8 NULL,                           -- 删除日期
    state        INT1             NOT NULL DEFAULT 0, -- 文件处理状态，0：待处理 1：处理完成 2：处理出错，比如视频文件，需要转码；图片需要获取尺寸等信息
    stateMsg     TEXT NULL                            -- 文件处理出错信息
);
CREATE INDEX index_userId ON dfs_file (userId);
CREATE INDEX index_parentId ON dfs_file (parentId);
CREATE INDEX index_name ON dfs_file (name);
CREATE INDEX index_isExtraON dfs_file (isExtra);
