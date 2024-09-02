package cn.dairo.dfs.interceptor


import cn.dairo.dfs.extension.toJson
import cn.dairo.dfs.sync.SyncLogUtil
import cn.dairo.dfs.util.DBID
import cn.dairo.lib.server.dbtool.DBBase
import cn.dairo.lib.server.dbtool.SqliteTool
import org.apache.ibatis.executor.Executor
import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.plugin.Interceptor
import org.apache.ibatis.plugin.Intercepts
import org.apache.ibatis.plugin.Invocation
import org.apache.ibatis.plugin.Signature
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import kotlin.concurrent.thread

/**
 * @author Badboy
 * 后台管理员权限验证拦截器
 */
@Component
@Intercepts(
    Signature(type = Executor::class, method = "update", args = [MappedStatement::class, Object::class]),
    //Signature(type = Executor::class, method = "query", args = [MappedStatement::class, Object::class])
)
class MybatisInterceptor : Interceptor {

    /**
     * 文件路径
     */
    @Value("\${sqlite.path}")
    private lateinit var dbPath: String

    /**
     * sqlite数据库连接
     */
    private val db: DBBase by lazy {
        SqliteTool(this.dbPath)
    }

    override fun intercept(invocation: Invocation): Any? {
        val result = invocation.proceed()

        //SQL语句执行完成之后，再保存日志，sql执行出错之后没有必要保存
        this.saveLog(invocation)
        return result
    }

    /**
     *将日志保存到数据库
     */
    private fun saveLog(invocation: Invocation) {
        val mappedStatement = invocation.args[0] as MappedStatement
        val boundSql = mappedStatement.getBoundSql(invocation.args[1])

        val parameterObject = boundSql.parameterObject

        //得到参数列表
        val params = boundSql.parameterMappings

        val paramValues = Array<Any?>(params.size) { null }
        params.forEachIndexed { index, item ->
            val key = item.property
            var value: Any?
            if (key == "0") {//只一个参数,并且参数用0来表示了
                value = parameterObject
            } else if (parameterObject is Map<*, *>) {//参数是一个HashMap类型
                value = parameterObject[key]
            } else {//参数是一个对象时
                val filed = parameterObject::class.java.getDeclaredField(key)
                filed.isAccessible = true
                value = filed.get(parameterObject)
            }
            if (value is Date) {//日期类型时,转换成时间戳再传输
                value = value.time
            }
            paramValues[index] = value
        }

        //将参数转换成JSON字符串
        val paramJson = paramValues.toJson

        //得到执行的sql文
        val sqlLog = boundSql.sql
        this.db.exec(
            "insert into sql_log(id,date,sql,param,state,source) values(?,?,?,?,?,?)",
            DBID.id,
            System.currentTimeMillis(),
            sqlLog,
            paramJson,
            1,
            "0.0.0.0"
        )
        //SyncSlave.instant.doSync()
        thread {
            SyncLogUtil.sendNotify()
        }
    }
}
