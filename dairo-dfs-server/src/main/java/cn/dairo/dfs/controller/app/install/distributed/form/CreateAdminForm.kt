package cn.dairo.dfs.controller.app.install.distributed.form

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

class CreateAdminForm {

    /**
     * 用户名
     */
    @Length(min = 2, max = 32)
    @NotBlank
    var name: String? = null

    /**
     * 登录密码
     */
    @Length(min = 4, max = 32)
    @NotBlank
    var pwd: String? = null
}