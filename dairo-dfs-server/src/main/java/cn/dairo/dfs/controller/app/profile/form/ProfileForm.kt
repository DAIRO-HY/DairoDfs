package cn.dairo.dfs.controller.app.profile.form

import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

class ProfileForm {

    /**
     * 是否开启分布式部署
     */
    var hasDistributed: Boolean? = null

    /**
     * 将当前服务器设置为只读,仅作为备份使用
     */
    var hasReadOnly: Boolean? = null

    /**
     * 文件上传限制
     */
    @Digits(integer = 11, fraction = 0)
    @NotBlank
    var uploadMaxSize: String? = null

    /**
     * 存储目录
     */
    @NotBlank
    var folders: String? = null

    /**
     * 主动同步时间间隔
     */
    @Min(1)
    var syncTimer: Int? = null

    /**
     * 同步域名
     */
    var syncDomains: String? = null
}