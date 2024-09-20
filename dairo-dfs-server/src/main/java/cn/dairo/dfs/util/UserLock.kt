package cn.dairo.dfs.util

/**
 * 用户操作锁
 * 防止某个用户的某些操作并发执行
 */
object UserLock {
    private val lockMap = HashMap<Long, Any>()

    /**
     * 获取锁
     */
    val Long.lock: Any
        get() = synchronized(UserLock.lockMap) {
            var lock = UserLock.lockMap[this]
            if (lock == null) {
                lock = Any()
                UserLock.lockMap[this] = lock
            }
            return lock
        }
}