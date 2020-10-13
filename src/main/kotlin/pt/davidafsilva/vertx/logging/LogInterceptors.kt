package pt.davidafsilva.vertx.logging

object LogInterceptors {
    private val noInterceptors: List<LogInterceptor> = emptyList()
    private val allLogLevels = LogLevel.values()
    private val interceptors = mutableMapOf<LogLevel, MutableList<LogInterceptor>>()

    fun register(logInterceptor: LogInterceptor) {
        allLogLevels.forEach { register(it, logInterceptor) }
    }

    fun register(vararg logInterceptors: LogInterceptor) {
        logInterceptors.forEach(::register)
    }

    fun register(level: LogLevel, logInterceptor: LogInterceptor) {
        interceptors.computeIfAbsent(level) { mutableListOf() }
            .add(logInterceptor)
    }

    fun register(level: LogLevel, vararg logInterceptors: LogInterceptor) {
        logInterceptors.forEach { register(level, it) }
    }

    fun interceptorsFor(level: LogLevel): List<LogInterceptor> = interceptors[level] ?: noInterceptors
}
