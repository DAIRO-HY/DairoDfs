package cn.dairo.make.client.api.source.form_to_model

import java.io.File

/**
 * 将代码转存相应平台的Modal代码
 */
object FormToModelUtil {

    /**
     * model文件存放目录
     */
    private const val FORM_FOLDER = "./dairo-dfs-server/src/main/java/cn/dairo/dfs/controller/app"

    /**
     * 开始
     */
    fun start() {
        loopFindFormFile(File(FORM_FOLDER))
    }

    /**
     * 循环查找Form文件列表
     * @param formFolder Form表单所在文件夹
     */
    private fun loopFindFormFile(formFolder: File) {
        formFolder.listFiles()?.forEach {
            if (it.isFile) {
                if (!it.name.endsWith("Form.kt")) {
                    return@forEach
                }

                //生成Swift的Model类
                FormToDartModelUtil.start(it)
            } else {
                loopFindFormFile(it)
            }
        }
    }
}