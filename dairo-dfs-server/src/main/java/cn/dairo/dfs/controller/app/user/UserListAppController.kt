package cn.dairo.dfs.controller.app.user

import cn.dairo.dfs.controller.app.user.form.UserListForm
import cn.dairo.dfs.controller.base.AppBase
import cn.dairo.dfs.dao.UserDao
import cn.dairo.dfs.extension.format
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

/**
 * 用户列表
 */
@Controller
@RequestMapping("/app/user_list")
class UserListAppController : AppBase() {

    /**
     * 用户操作Dao
     */
    @Autowired
    private lateinit var userDao: UserDao

    /**
     * 页面初始化
     */
    @PostMapping
    @ResponseBody
    fun init(): List<UserListForm> {
        val dtoList = this.userDao.getAll()
        val userList = dtoList.map {
            UserListForm().apply {
                this.id = it.id
                this.name = it.name
                this.email = it.email
                this.date = it.date?.format()
                this.state = if (it.state == 1) "启用" else "禁用"
            }
        }
        return userList
    }
}
