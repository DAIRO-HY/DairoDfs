package cn.dairo.lib.uc.auto.annotations

/**
 * 分页查询数据时指定的表名
 * @param table 表名
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
annotation class UCTable(val table: String)
