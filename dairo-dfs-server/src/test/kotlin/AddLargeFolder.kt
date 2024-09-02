import cn.dairo.lib.server.dbtool.SqliteTool

object AddLargeFolder {
    @JvmStatic
    fun main(args: Array<String>) {
        SqliteTool("./data/dairo-dfs.sqlite").use { db ->
            var id = 0
            repeat(10) {
                id++
                val curentId1 = id
                db.exec("insert into dfs_folder(parentId,name)values(0,'${it}')")
                repeat(10) {
                    id++
                    val curentId2 = id
                    db.exec("insert into dfs_folder(parentId,name)values(${curentId1},'${it}')")
                    repeat(10) {
                        id++
                        val curentId3 = id
                        db.exec("insert into dfs_folder(parentId,name)values(${curentId2},'${it}')")
                        repeat(10) {
                            id++
                            val curentId4 = id
                            db.exec("insert into dfs_folder(parentId,name)values(${curentId3},'${it}')")
                            repeat(10) {
                                id++
                                val curentId5 = id
                                db.exec("insert into dfs_folder(parentId,name)values(${curentId4},'${it}')")
                                println(id)
                            }
                        }
                    }
                }
            }
        }
    }
}