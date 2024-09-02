package cn.dairo.dfs.controller.app.init.form

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

class InitForm {

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