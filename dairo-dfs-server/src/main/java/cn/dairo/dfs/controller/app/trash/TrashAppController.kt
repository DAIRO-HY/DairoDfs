package cn.dairo.dfs.controller.app.trash

import cn.dairo.dfs.code.ErrorCode
import cn.dairo.dfs.config.SystemConfig
import cn.dairo.dfs.controller.app.trash.form.TrashForm
import cn.dairo.dfs.controller.base.AppBase
import cn.dairo.dfs.dao.DfsFileDao
import cn.dairo.dfs.extension.isFile
import cn.dairo.dfs.extension.toDataSize
import cn.dairo.dfs.service.DfsFileDeleteService
import cn.dairo.dfs.service.DfsFileService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

/**
 * 垃圾桶文件列表
 */
@Controller
@RequestMapping("/app/trash")
class TrashAppController : AppBase() {

    @Value("\${config.trash-timeout}")
    private var trashTimeout = 0L

    /**
     * 文件夹数据操作Service
     */
    @Autowired
    private lateinit var dfsFileService: DfsFileService

    /**
     * 文件彻底操作Service
     */
    @Autowired
    private lateinit var dfsFileDeleteService: DfsFileDeleteService

    /**
     * 文件操作Dao
     */
    @Autowired
    private lateinit var dfsFileDao: DfsFileDao

    @Operation(summary = "获取回收站文件列表")
    @PostMapping("/get_list")
    @ResponseBody
    fun getList(): List<TrashForm> {
        val userId = super.loginId
        val now = System.currentTimeMillis()
        val trashSaveTime = this.trashTimeout * 24 * 60 * 60 * 1000
        val list = this.dfsFileDao.getDeleteList(userId).map {
            val deleteDate = it.deleteDate!!

            //剩余删除时间
            val time = trashSaveTime - (now - deleteDate)

            val deleteLastTime: String
            if (time < 0) {
                deleteLastTime = "即将删除"
            } else {
                if (time > 24 * 60 * 60 * 1000) {//超过1天
                    deleteLastTime = "${time / (24 * 60 * 60 * 1000)}天后删除"
                } else if (time > 60 * 60 * 1000) {//超过1小时
                    deleteLastTime = "${time / (60 * 60 * 1000)}小时后删除"
                } else if (time > 60 * 1000) {//超过1分钟
                    deleteLastTime = "${time / (60 * 1000)}分钟后删除"
                } else {
                    deleteLastTime = "即将删除"
                }
            }
            TrashForm().apply {
                this.id = it.id
                this.name = it.name
                this.size = it.size.toDataSize
                this.date = deleteLastTime
                this.fileFlag = it.isFile
            }
        }
        return list
    }

    @Operation(summary = "彻底删除文件")
    @PostMapping("/logic_delete")
    @ResponseBody
    fun logicDelete(
        @Parameter(description = "选中的文件ID列表") @RequestParam("ids", required = true) ids: List<Long>
    ) {
        val userId = super.loginId
        ids.forEach {//验证是否有删除权限
            val fileDto = this.dfsFileDao.getOne(it)!!
            if (fileDto.userId != userId) {//非自己的文件，无法删除
                throw ErrorCode.NOT_ALLOW
            }
            if (fileDto.deleteDate == null) {//该文件未标记为删除
                throw ErrorCode.NOT_ALLOW
            }
        }
        this.dfsFileDeleteService.addDelete(ids)
    }

    @Operation(summary = "从垃圾箱还原文件")
    @PostMapping("/trash_recover")
    @ResponseBody
    fun trashRecover(
        @Parameter(description = "选中的文件ID列表") @RequestParam(
            "ids",
            required = true
        ) ids: List<Long>
    ) {
        val userId = super.loginId
        this.dfsFileService.trashRecover(userId, ids)
    }
}
