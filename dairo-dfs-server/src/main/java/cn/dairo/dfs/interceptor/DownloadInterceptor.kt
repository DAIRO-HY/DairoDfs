package cn.dairo.dfs.interceptor

import cn.dairo.dfs.dao.DfsFileDao
import cn.dairo.dfs.dao.UserDao
import cn.dairo.dfs.extension.fileName
import cn.dairo.dfs.extension.fileParent
import cn.dairo.dfs.service.DfsFileService
import cn.dairo.dfs.util.DfsFileUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.HandlerInterceptor

/**
 * @author Badboy
 * 后台管理员权限验证拦截器
 */
class DownloadInterceptor : HandlerInterceptor {


    /**
     * 用户数据操作DAO
     */
    @Autowired
    private lateinit var userDao: UserDao


    /**
     * 文件数据操作Service
     */
    @Autowired
    private lateinit var dfsFileService: DfsFileService

    /**
     * 文件数据操作Dao
     */
    @Autowired
    private lateinit var dfsFileDao: DfsFileDao
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val urlPath = request.servletPath

        //去掉前面的/d/后为文件的路径
        var path = urlPath.substring(3)
        val firstPathIndex = path.indexOf("/")

        //得到用户私有访问路径
        val userPath = path.substring(0, firstPathIndex)

        //得到用户信息
        val userId = this.userDao.selectIdByUrlPath(userPath)
        if (userId == null) {
            response.status = HttpStatus.NOT_FOUND.value()
            return false
        }
        path = path.substring(firstPathIndex)

        val folder = path.fileParent
        val folderId = this.dfsFileService.getIdByFolder(userId, folder)
        if (folderId == null) {//文件夹不存在
            response.status = HttpStatus.NOT_FOUND.value()
            return false
        }

        //文件名
        val name = path.fileName
        val dfsFile = this.dfsFileDao.getByParentIdAndName(userId, folderId, name)
        if (dfsFile == null) {//文件不存在
            response.status = HttpStatus.NOT_FOUND.value()
            return false
        }
        try {
            count++
            DfsFileUtil.download(dfsFile.id!!, request, response)
        } finally {
            count--
        }
        return false
    }

    companion object {
        @Volatile
        var count = 0
    }
}
