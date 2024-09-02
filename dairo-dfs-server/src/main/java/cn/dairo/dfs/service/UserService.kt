package cn.dairo.dfs.service

import cn.dairo.dfs.config.Constant
import cn.dairo.dfs.dao.UserDao
import cn.dairo.dfs.dao.dto.UserDto
import cn.dairo.dfs.util.DBID
import cn.dairo.lib.StringUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

/**
 * 用户操作Service
 */
@Service
class UserService {

    /**
     * 用户数据操作Dao
     */
    @Autowired
    private lateinit var userDao: UserDao

    /**
     * 添加一个用户
     * @param dto 用户Dto
     */
    fun add(dto: UserDto) {
        if (dto.pwd == null) {//生成临时密码
            dto.pwd = Constant.NO_SET_PWD_PRE + StringUtil.getRandomNum(6)
        }
        dto.date = Date()
        dto.id = DBID.id
        this.userDao.add(dto)
    }
}
