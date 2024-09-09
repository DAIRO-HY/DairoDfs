package cn.dairo.dfs.sync.bean

class SyncInfo {

    /**
     * 主机域名
     */
    var domain: String? = null

    /**
     * 同步状态 0：待机中   1：同步中  2：同步错误
     */
    var state = 0

    /**
     * 同步消息
     */
    var msg: String? = null

    /**
     * 本次同步数量
     */
    var syncCount = 0
}