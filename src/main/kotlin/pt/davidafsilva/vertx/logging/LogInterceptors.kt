package pt.davidafsilva.vertx.logging

import org.slf4j.event.Level

object LogInterceptors {
    private val allLogLevels = Level.values()
    private val interceptors = mutableMapOf<Level, MutableList<LogInterceptor>>()
        .withDefault { mutableListOf() }

    fun register(logInterceptor: LogInterceptor) {
        allLogLevels.forEach { register(it, logInterceptor) }
    }

    fun register(level: Level, logInterceptor: LogInterceptor) {
        interceptors[level]!!.add(logInterceptor)
    }

    fun register(level: Level, vararg logInterceptors: LogInterceptor) {
        logInterceptors.forEach { interceptors[level]!!.add(it) }
    }

    fun interceptorsFor(level: Level): List<LogInterceptor> = interceptors[level]!!
}
