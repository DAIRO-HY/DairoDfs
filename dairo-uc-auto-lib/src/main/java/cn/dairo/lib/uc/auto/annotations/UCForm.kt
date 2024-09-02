package cn.dairo.lib.uc.auto.annotations

/**
 * 将字段标记为文本
 * @param title 显示标题
 */
@Target(AnnotationTarget.FIELD)
annotation class UCForm(
    val title: String,
    val type: String = TEXT,
    val disabled: Boolean = false,
    val `class`: String = "",
    val hint: String = "",
    val showBy: String = ""
) {
    companion object {

        /**
         * 文本输入
         */
        const val TEXT = "TEXT"

        /**
         * 密码输入框
         */
        const val PASSWORD = "PASSWORD"

        /**
         * 复选框
         */
        const val CHECKBOX = "CHECKBOX"

        /**
         * 单选框
         */
        const val RADIOBOX = "RADIOBOX"

        /**
         * 下拉列表框
         */
        const val SELECT = "SELECT"

        /**
         * 隐藏域
         */
        const val HIDDEN = "HIDDEN"

        /**
         * 图片
         */
        const val IMAGE = "IMAGE"
    }
}
