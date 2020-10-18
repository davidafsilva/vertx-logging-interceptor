package pt.davidafsilva.vertx.logging

object LogInterceptors {
    private val noInterceptors: List<LogInterceptor> = emptyList()
    private val allLogLevels = LogLevel.values()
    private val interceptors = mutableMapOf<LogLevel, MutableList<LogInterceptor>>()

    @JvmStatic
    fun register(logInterceptor: LogInterceptor) {
        allLogLevels.forEach { register(it, logInterceptor) }
    }

    @JvmStatic
    fun register(vararg logInterceptors: LogInterceptor) {
        logInterceptors.forEach(::register)
    }

    @JvmStatic
    fun register(level: LogLevel, logInterceptor: LogInterceptor) {
        interceptors.computeIfAbsent(level) { mutableListOf() }
            .add(logInterceptor)
    }

    @JvmStatic
    fun register(level: LogLevel, vararg logInterceptors: LogInterceptor) {
        logInterceptors.forEach { register(level, it) }
    }

    internal fun interceptorsFor(level: LogLevel): List<LogInterceptor> = interceptors[level] ?: noInterceptors
}
