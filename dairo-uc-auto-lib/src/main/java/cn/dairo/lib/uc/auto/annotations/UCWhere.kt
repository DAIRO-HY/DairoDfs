package cn.dairo.lib.uc.auto.annotations

/**
 * sql语句查询条件注解
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
annotation class UCWhere(val where: String = EQUAL, val concat: String = CONCAT_AND) {
    companion object {

        /**
         * 相等判断
         */
        const val EQUAL = "{FIELD} = ?"

        /**
         * 前后模糊匹配
         */
        const val LIKE = "{FIELD} like concat('%',?,'%')"

        /**
         * 前缀匹配
         */
        const val LIKE_START = "{FIELD} like concat(?,'%')"

        /**
         * 后缀匹配
         */
        const val LIKE_END = "{FIELD} like concat('%',?)"

        /**
         * 条件AND连接
         */
        const val CONCAT_AND = "and"


        /**
         * 条件OR连接
         */
        const val CONCAT_OR = "or"

    }
}
