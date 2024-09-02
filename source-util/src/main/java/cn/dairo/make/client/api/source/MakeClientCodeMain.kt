package cn.dairo.make.client.api.source

import cn.dairo.make.client.api.source.controller_to_api.ParseController
import cn.dairo.make.client.api.source.form_to_model.FormToModelUtil

object MakeClientCodeMain {
    @JvmStatic
    fun main(args: Array<String>) {
        FormToModelUtil.start()
        ParseController.start()
    }
}