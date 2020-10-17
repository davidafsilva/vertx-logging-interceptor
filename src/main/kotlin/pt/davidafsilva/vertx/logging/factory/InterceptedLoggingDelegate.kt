package pt.davidafsilva.vertx.logging.factory

import io.vertx.core.spi.logging.LogDelegate
import pt.davidafsilva.vertx.logging.LogInterceptor
import pt.davidafsilva.vertx.logging.LogLevel
import pt.davidafsilva.vertx.logging.LogPropagation

internal class InterceptedLoggingDelegate(
    private val loggerName: String,
    private val delegate: LogDelegate,
    private val levelInterceptors: (LogLevel) -> List<LogInterceptor>
) : LogDelegate by delegate {

    override fun fatal(message: Any?) {
        intercept(LogLevel.ERROR, message) { delegate.fatal(message) }
    }

    override fun fatal(message: Any?, t: Throwable?) {
        intercept(LogLevel.ERROR, message, t) { delegate.fatal(message, t) }
    }

    override fun error(message: Any?) {
        intercept(LogLevel.ERROR, message) { delegate.error(message) }
    }

    override fun error(message: Any?, vararg params: Any?) {
        intercept(LogLevel.ERROR, message) { delegate.error(message, *params) }
    }

    override fun error(message: Any?, t: Throwable?) {
        intercept(LogLevel.ERROR, message, t) { delegate.error(message, t) }
    }

    override fun error(message: Any?, t: Throwable?, vararg params: Any?) {
        intercept(LogLevel.ERROR, message, t) { delegate.error(message, t, *params) }
    }

    override fun warn(message: Any?) {
        intercept(LogLevel.WARN, message) { delegate.error(message) }
    }

    override fun warn(message: Any?, vararg params: Any?) {
        intercept(LogLevel.WARN, message) { delegate.error(message, *params) }
    }

    override fun warn(message: Any?, t: Throwable?) {
        intercept(LogLevel.WARN, message, t) { delegate.error(message, t) }
    }

    override fun warn(message: Any?, t: Throwable?, vararg params: Any?) {
        intercept(LogLevel.WARN, message, t) { delegate.error(message, t, *params) }
    }

    override fun info(message: Any?) {
        intercept(LogLevel.INFO, message) { delegate.error(message) }
    }

    override fun info(message: Any?, vararg params: Any?) {
        intercept(LogLevel.INFO, message) { delegate.error(message, *params) }
    }

    override fun info(message: Any?, t: Throwable?) {
        intercept(LogLevel.INFO, message, t) { delegate.error(message, t) }
    }

    override fun info(message: Any?, t: Throwable?, vararg params: Any?) {
        intercept(LogLevel.INFO, message, t) { delegate.error(message, t, *params) }
    }

    override fun debug(message: Any?) {
        intercept(LogLevel.DEBUG, message) { delegate.error(message) }
    }

    override fun debug(message: Any?, vararg params: Any?) {
        intercept(LogLevel.DEBUG, message) { delegate.error(message, *params) }
    }

    override fun debug(message: Any?, t: Throwable?) {
        intercept(LogLevel.DEBUG, message, t) { delegate.error(message, t) }
    }

    override fun debug(message: Any?, t: Throwable?, vararg params: Any?) {
        intercept(LogLevel.DEBUG, message, t) { delegate.error(message, t, *params) }
    }

    override fun trace(message: Any?) {
        intercept(LogLevel.TRACE, message) { delegate.error(message) }
    }

    override fun trace(message: Any?, vararg params: Any?) {
        intercept(LogLevel.TRACE, message) { delegate.error(message, *params) }
    }

    override fun trace(message: Any?, t: Throwable?) {
        intercept(LogLevel.TRACE, message, t) { delegate.error(message, t) }
    }

    override fun trace(message: Any?, t: Throwable?, vararg params: Any?) {
        intercept(LogLevel.TRACE, message, t) { delegate.error(message, t, *params) }
    }

    private inline fun intercept(
        level: LogLevel,
        message: Any?,
        throwable: Throwable? = null,
        parameters: Array<Any?>? = null,
        logging: () -> Unit
    ) {
        val propagateLog = levelInterceptors(level).all { interceptor ->
            val result = when {
                throwable == null && parameters == null -> interceptor.intercept(loggerName, message)
                parameters == null -> interceptor.intercept(loggerName, message, throwable!!)
                throwable == null -> interceptor.intercept(loggerName, message, parameters)
                else -> interceptor.intercept(loggerName, message, throwable, parameters)
            }
            result == LogPropagation.CONTINUE
        }
        if (propagateLog) logging()
    }
}
