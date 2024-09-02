package cn.dairo.dfs.controller.app.my_share

import cn.dairo.dfs.code.ErrorCode
import cn.dairo.dfs.controller.app.my_share.form.MyShareDetailForm
import cn.dairo.dfs.controller.app.my_share.form.MyShareForm
import cn.dairo.dfs.controller.base.AppBase
import cn.dairo.dfs.dao.ShareDao
import cn.dairo.dfs.extension.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * 提取分享的文件
 */
@Controller
@RequestMapping("/app/my_share")
class MyShareAppController : AppBase() {

    /**
     * 分享操作Dao
     */
    @Autowired
    private lateinit var shareDao: ShareDao


    /**
     * 获取所有的分享
     */
    @PostMapping("/get_list")
    @ResponseBody
    fun getList(): List<MyShareForm> {
        val userId = super.loginId
        val now = System.currentTimeMillis()
        val list = this.shareDao.getByUser(userId).map {
            val names = it.names!!.split("|")
            val name = if (names.size == 1) {
                it.names!!
            } else {
                names[0] + "等多个文件"
            }
            val endDate: String
            if (it.endDate == null) {
                endDate = "永久有效"
            } else {
                val endTime = it.endDate!! - now
                if (endTime < 0) {
                    endDate = "已过期"
                } else {
                    if (endTime > 24 * 60 * 60 * 1000) {//超过1天
                        endDate = "${endTime / (24 * 60 * 60 * 1000)}天后过期"
                    } else if (endTime > 60 * 60 * 1000) {//超过1小时
                        endDate = "${endTime / (60 * 60 * 1000)}小时后过期"
                    } else if (endTime > 60 * 1000) {//超过1分钟
                        endDate = "${endTime / (60 * 1000)}分钟后过期"
                    } else {
                        endDate = "马上过期"
                    }
                }
            }
            val form = MyShareForm()
            form.id = it.id
            form.name = name
            form.multipleFlag = names.size > 1
            form.endDate = endDate
            form.date = it.date!!.format()
            form
        }
        return list
    }

    /**
     * 获取分享详细
     */
    @PostMapping("/get_detail")
    @ResponseBody
    fun getDetail(id: String): MyShareDetailForm {
        val userId = super.loginId
        val shareDto = this.shareDao.getOne(id)!!
        if (shareDto.userId != userId) {
            throw ErrorCode.NOT_ALLOW
        }
        var folder = shareDto.folder!!
        if (folder.isEmpty()) {
            folder = "/"
        }
        val endDate = if (shareDto.endDate == null) {
            "永久有效"
        } else {
            Date(shareDto.endDate!!).format()
        }

        val form = MyShareDetailForm()
        form.id = shareDto.id
        form.names = shareDto.names
        form.date = shareDto.date!!.format()
        form.endDate = endDate
        form.pwd = shareDto.pwd ?: "无"
        form.folder = folder
        return form
    }


    /**
     * 取消所选分享
     * @param ids 选择的id数组
     */
    @PostMapping("/delete")
    @ResponseBody
    fun delete(ids: Array<Long>) {
        val userId = super.loginId
        this.shareDao.delete(userId, ids.toList())
    }
}
