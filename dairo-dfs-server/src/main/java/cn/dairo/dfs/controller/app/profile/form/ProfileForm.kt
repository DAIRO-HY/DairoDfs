package cn.dairo.dfs.controller.app.profile.form

import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank

class ProfileForm {

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
     * 同步域名
     */
    var syncDomains: String? = null
}