package cn.dairo.dfs.util

object DBID {

    /**
     * 生成数据库主键ID
     */
    val id: Long
        get() {
            synchronized(this) {
                val idStr = System.currentTimeMillis().toString() + String.format("%03d", (Math.random() * 100).toInt())
                return idStr.toLong()
            }
        }
}