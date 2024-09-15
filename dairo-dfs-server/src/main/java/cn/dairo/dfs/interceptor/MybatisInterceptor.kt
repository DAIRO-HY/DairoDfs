package cn.dairo.dfs.interceptor


import cn.dairo.dfs.config.SystemConfig
import cn.dairo.dfs.controller.distributed.DistributedController
import cn.dairo.dfs.exception.BusinessException
import cn.dairo.dfs.extension.bean
import cn.dairo.dfs.extension.toJson
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
 * SQL语句拦截器
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
//    @Autowired
//    private lateinit var syncController: SyncController

    /**
     * 记录最后一次添加的日志ID
     */
    var lastID = 0L

    /**
     * sqlite数据库连接
     */
    private val db: DBBase by lazy {
        SqliteTool(this.dbPath)
    }

    override fun intercept(invocation: Invocation): Any? {
        if (SystemConfig.instance.isReadOnly) {//这是一个只读服务器
            throw BusinessException("只读服务,不允许该操作。")
        }
        val result = invocation.proceed()
        if (SystemConfig.instance.openSqlLog) {//如果有开启分布式部署

            //SQL语句执行完成之后，再保存日志，sql执行出错之后没有必要保存
            this.saveLog(invocation)
        }
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

        val id = DBID.id
        this.lastID = id
        this.db.exec(
            "insert into sql_log(id,date,sql,param,state,source) values(?,?,?,?,?,?)",
            id,
            System.currentTimeMillis(),
            sqlLog,
            paramJson,
            1,
            "0.0.0.0"
        )
        thread {
            //SyncLogUtil.sendNotify()
//            this.syncController.push()
            DistributedController::class.bean.push()
        }
    }
}
