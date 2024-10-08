package cn.dairo.dfs.controller.app.install.create_admin

import cn.dairo.dfs.code.ErrorCode
import cn.dairo.dfs.controller.app.install.create_admin.form.CreateAdminForm
import cn.dairo.dfs.controller.base.AppBase
import cn.dairo.dfs.dao.UserDao
import cn.dairo.dfs.dao.dto.UserDto
import cn.dairo.dfs.extension.md5
import cn.dairo.dfs.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

/**
 * 管理员账号初始化
 */
@Controller
@RequestMapping("/app/install/create_admin")
class CreateAdminAppController : AppBase() {

    /**
     * 用户操作Dao
     */
    @Autowired
    private lateinit var userDao: UserDao

    /**
     * 用户操作Service
     */
    @Autowired
    private lateinit var userService: UserService

    /**
     * 页面初始化
     */
    @GetMapping
    fun execute(): String {
        if (this.userDao.selectOne(1) != null) {
            return "redirect:/app/login"
        }
        return "app/install/create_admin"
    }

    /**
     * 账号初始化API
     */
    @PostMapping("/add_admin")
    @ResponseBody
    fun addAdmin(@Validated form: CreateAdminForm) {
        if (this.userDao.selectOne(1) != null) {//管理员用户只能被创建一次
            throw ErrorCode.NOT_ALLOW
        }
        val userDto = UserDto()
        userDto.name = form.name
        userDto.pwd = form.pwd!!.md5
        userDto.state = 1
        this.userService.add(userDto)
    }
}
