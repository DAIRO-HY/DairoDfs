package cn.dairo.dfs.controller.app.user.form

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

class UserEditForm {

    /**
     * 主键
     */
    var id: Long? = null

    /**
     * 用户名
     */
    @Length(min = 2, max = 32)
    var name: String? = null

    /**
     * 用户电子邮箱
     */
    @NotBlank
    @Email
    var email: String? = null

    /**
     * 用户状态
     */
    var state: Int? = null

    /**
     * 创建日期
     */
    var date: String? = null
}
