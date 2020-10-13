package pt.davidafsilva.vertx.logging

import org.slf4j.event.Level

object LogInterceptors {
    private val noInterceptors: List<LogInterceptor> = emptyList()
    private val allLogLevels = Level.values()
    private val interceptors = mutableMapOf<Level, MutableList<LogInterceptor>>()

    fun register(logInterceptor: LogInterceptor) {
        allLogLevels.forEach { register(it, logInterceptor) }
    }

    fun register(vararg logInterceptors: LogInterceptor) {
        logInterceptors.forEach(::register)
    }

    fun register(level: Level, logInterceptor: LogInterceptor) {
        interceptors.computeIfAbsent(level) { mutableListOf() }
            .add(logInterceptor)
    }

    fun register(level: Level, vararg logInterceptors: LogInterceptor) {
        logInterceptors.forEach { register(level, it) }
    }

    fun interceptorsFor(level: Level): List<LogInterceptor> = interceptors[level] ?: noInterceptors
}
