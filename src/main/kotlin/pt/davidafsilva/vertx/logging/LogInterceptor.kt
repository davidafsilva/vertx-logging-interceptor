package pt.davidafsilva.vertx.logging

import pt.davidafsilva.vertx.logging.LogPropagation.CONTINUE

interface LogInterceptor {
    fun intercept(logger: String, message: Any?): LogPropagation
    fun intercept(logger: String, message: Any?, params: Array<Any?>): LogPropagation
    fun intercept(logger: String, message: Any?, t: Throwable): LogPropagation
    fun intercept(logger: String, message: Any?, t: Throwable, params: Array<Any?>): LogPropagation
}

interface NoOpLogInterceptor : LogInterceptor {
    override fun intercept(logger: String, message: Any?): LogPropagation = CONTINUE
    override fun intercept(logger: String, message: Any?, params: Array<Any?>): LogPropagation = CONTINUE
    override fun intercept(logger: String, message: Any?, t: Throwable): LogPropagation = CONTINUE
    override fun intercept(logger: String, message: Any?, t: Throwable, params: Array<Any?>): LogPropagation = CONTINUE
}

enum class LogPropagation { BLOCK, CONTINUE }
