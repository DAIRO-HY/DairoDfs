package cn.dairo.dfs.controller.app.sync

import cn.dairo.dfs.controller.base.AppBase
import cn.dairo.dfs.dao.SqlLogDao
import cn.dairo.dfs.dao.dto.SqlLogDto
import cn.dairo.dfs.sync.SyncByTable
import cn.dairo.dfs.sync.SyncByLog
import cn.dairo.dfs.sync.bean.SyncInfo
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import kotlin.concurrent.thread

/**
 * 数据同步状态
 */
@Controller
@RequestMapping("/app/sync")
class SyncAppController : AppBase() {

    /**
     * sql日志操作
     */
    private lateinit var sqlLogDao: SqlLogDao

    /**
     * 页面数据初始化
     */
    @PostMapping("/info_list")
    @ResponseBody
    fun infoList(): List<SyncInfo> {
//        val infoList = SyncLogUtil.syncInfoList.map {
//            val form = SyncForm()
//            form.domain = it.domain
//            form.state = when (it.state) {
//                0 -> "待机中"
//                1 -> "同步中"
//                2 -> "同步错误"
//                else -> ""
//            }
//            form.msg = it.msg
//            form
//        }
//        return infoList
        return SyncByLog.syncInfoList
    }

    /**
     * 日志同步
     */
    @PostMapping("/sync")
    @ResponseBody
    fun sync() {
//        SyncController::class.bean.push()
        thread {
            SyncByLog.start(true)
        }
    }

    /**
     * 全量同步
     */
    @PostMapping("/sync_all")
    @ResponseBody
    fun syncAll() {
        thread {
            SyncByTable.start(true)
        }
    }

    /**
     * 获取错误的日志记录
     */
    fun getErrorLog(): SqlLogDto? {
        return this.sqlLogDao.getErrorLog()
    }
}
