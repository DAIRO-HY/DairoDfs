package cn.dairo.dfs.controller.sync

import cn.dairo.dfs.config.Constant
import cn.dairo.dfs.controller.base.AppBase
import cn.dairo.dfs.dao.DfsFileDao
import cn.dairo.dfs.dao.LocalFileDao
import cn.dairo.dfs.exception.BusinessException
import cn.dairo.dfs.sync.SyncAllUtil
import cn.dairo.dfs.sync.SyncLogUtil
import cn.dairo.dfs.util.DBID
import cn.dairo.dfs.util.DfsFileUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import kotlin.concurrent.thread

/**
 * 数据同步处理Controller
 */
@Controller
@RequestMapping("/sync/{token}")
class SyncController : AppBase() {

    /**
     * 文件数据操作Dao
     */
    @Autowired
    private lateinit var dfsFileDao: DfsFileDao

    /**
     * 本地存储文件数据操作Dao
     */
    @Autowired
    private lateinit var localFileDao: LocalFileDao

    /**
     * 获取sql日志
     */
    @GetMapping("/get_log")
    @ResponseBody
    fun getLog(lastId: Long): List<Map<String, Any?>> {
        val logList = Constant.dbService.selectList(
            "select id,date,sql,param from sql_log where id > ? order by id limit 10000", lastId
        )
        return logList
    }

    /**
     * 主机发起同步通知
     */
    @GetMapping("/push_notify")
    @ResponseBody
    fun pushNotify() {
        thread {
            SyncLogUtil.start()
        }
    }

    /**
     * 获取一个断面ID，防止再全量同步的过程中，主机又增加数据，导致全量同步数据不完整
     * 其实就是当前服务器时间戳
     */
    @GetMapping("/get_aop_id")
    @ResponseBody
    fun getAopId() = DBID.id

    /**
     * 获取每个表的id
     * @param tbName 表名
     * @param lastId 已经取到的最后一个id
     * @param aopId 断面ID
     */
    @GetMapping("/get_table_id")
    @ResponseBody
    fun getTableId(tbName: String, lastId: Long, aopId: Long): String {
        if (SyncAllUtil.isRuning || SyncLogUtil.isRuning) {
            throw BusinessException("主机正在同步数据中，请等待完成后继续。")
        }
        return Constant.dbService.selectList(
            String::class, "select id from $tbName where id > ? and id < ? order by id asc limit 1", lastId, aopId
        ).joinToString(separator = ",") { it }
    }

    /**
     * 获取表数据
     * @param tbName 表名
     * @param ids 要取的数据id列表
     */
    @GetMapping("/get_table_data")
    @ResponseBody
    fun getTableData(tbName: String, ids: String): List<*> {
        if (SyncAllUtil.isRuning || SyncLogUtil.isRuning) {
            throw BusinessException("主机正在同步数据中，请等待完成后继续。")
        }
        return Constant.dbService.selectList(
            "select * from $tbName where id in ($ids)"
        )
    }


    /**
     * 文件下载
     * @param request 客户端请求
     * @param response 往客户端返回内容
     * @param id 文件ID
     */
    @GetMapping("/download/{md5}")
    fun download(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @PathVariable md5: String
    ) {
        if (SyncAllUtil.isRuning || SyncLogUtil.isRuning) {
            throw BusinessException("主机正在同步数据中，请等待完成后继续。")
        }
        val localFileDto = this.localFileDao.selectByFileMd5(md5)
        if (localFileDto == null) {
            response.status = HttpStatus.NOT_FOUND.value()
            return
        }
        response.reset() //清除buffer缓存
        DfsFileUtil.download(localFileDto, request, response)
    }
}
