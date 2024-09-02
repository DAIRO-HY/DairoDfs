package cn.dairo.lib.uc.auto.annotations

/**
 * 表格标题信息注解
 * @param title 显示标题
 */
@Target(AnnotationTarget.FIELD)
annotation class UCColumn(
    val title: String,
    val orderable: Boolean = true,//是否允许排序
    val defaultSort: String = "",//默认排序方式
    val type: String = ""//显示类型
) {
    companion object {

        /**
         * 显示一张图片
         */
        const val TYPE_IMAGE = "IMAGE"
    }
}
