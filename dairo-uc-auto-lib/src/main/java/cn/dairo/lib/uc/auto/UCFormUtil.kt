package cn.dairo.lib.uc.auto

import cn.dairo.lib.uc.auto.annotations.UCForm
import cn.dairo.lib.uc.auto.annotations.UCOption
import cn.dairo.lib.uc.auto.bean.UCFormBean

/**
 * 生成页面表单字段数据
 */
object UCFormUtil {

    /**
     * 生成
     * @param form 要生的表单
     * @return 页面表单字段列表
     */
    fun make(form: Any): List<UCFormBean> {
        val formFieldList = ArrayList<UCFormBean>()
        form::class.java.declaredFields.forEach { field ->
            field.isAccessible = true

            //得到表单注解
            val formAnn = field.annotations.find { it is UCForm }
            formAnn ?: return@forEach
            formAnn as UCForm
            val fieldForm = UCFormBean()
            fieldForm.name = field.name//字段名
            fieldForm.title = formAnn.title//标题
            fieldForm.type = formAnn.type//表单类型
            if (formAnn.hint.isNotEmpty()) {//设置提示内容
                fieldForm.hint = formAnn.hint
            }
            if (formAnn.disabled) {//设置是否是只读属性
                fieldForm.disabled = true
            }
            if (formAnn.showBy.isNotEmpty()) {//显示条件
                fieldForm.showBy = formAnn.showBy
            }
            when (formAnn.type) {
                UCForm.CHECKBOX, UCForm.RADIOBOX, UCForm.SELECT -> {//复选框,单选框,下拉菜单

                    //从form中获取可选数据
                    val getOptionMethod = form::class.java.getDeclaredMethod(field.name + "Option")
                    getOptionMethod.isAccessible = true
                    fieldForm.option = getOptionMethod.invoke(form) as List<UCOption>

                }
            }

            //表单值
            val value = field.get(form)
            if (fieldForm.type == UCForm.CHECKBOX) {//复选框时的值必须的时数组形式
                if (value is List<*>) {
                    fieldForm.value = value
                } else if (value != null) {
                    fieldForm.value = listOf(value)
                } else {
                    fieldForm.value = ArrayList<Any>()
                }
            } else {
                fieldForm.value = value
            }
            formFieldList.add(fieldForm)
        }
        return formFieldList
    }
}
