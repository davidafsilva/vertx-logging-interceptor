package pt.davidafsilva.vertx.logging.factory

import io.vertx.core.spi.logging.LogDelegate
import pt.davidafsilva.vertx.logging.LogInterceptor
import pt.davidafsilva.vertx.logging.LogLevel
import pt.davidafsilva.vertx.logging.LogPropagation

internal class InterceptedLoggingDelegate(
    internal val loggerName: String,
    internal val delegate: LogDelegate,
    private val levelInterceptors: (LogLevel) -> List<LogInterceptor>
) : LogDelegate by delegate {

    override fun fatal(message: Any?) =
        intercept(LogLevel.ERROR, message) { delegate.fatal(message) }

    override fun fatal(message: Any?, t: Throwable?) =
        intercept(LogLevel.ERROR, message, t) { delegate.fatal(message, t) }

    override fun error(message: Any?) =
        intercept(LogLevel.ERROR, message) { delegate.error(message) }

    override fun error(message: Any?, vararg params: Any?) =
        intercept(LogLevel.ERROR, message, throwable = null, parameters = params) { delegate.error(message, *params) }

    override fun error(message: Any?, t: Throwable?) =
        intercept(LogLevel.ERROR, message, t) { delegate.error(message, t) }

    override fun error(message: Any?, t: Throwable?, vararg params: Any?) =
        intercept(LogLevel.ERROR, message, t, params) { delegate.error(message, t, *params) }

    override fun warn(message: Any?) =
        intercept(LogLevel.WARN, message) { delegate.warn(message) }

    override fun warn(message: Any?, vararg params: Any?) =
        intercept(LogLevel.WARN, message, throwable = null, parameters = params) { delegate.warn(message, *params) }

    override fun warn(message: Any?, t: Throwable?) =
        intercept(LogLevel.WARN, message, t) { delegate.warn(message, t) }

    override fun warn(message: Any?, t: Throwable?, vararg params: Any?) =
        intercept(LogLevel.WARN, message, t, params) { delegate.warn(message, t, *params) }

    override fun info(message: Any?) =
        intercept(LogLevel.INFO, message) { delegate.info(message) }

    override fun info(message: Any?, vararg params: Any?) =
        intercept(LogLevel.INFO, message, throwable = null, parameters = params) { delegate.info(message, *params) }

    override fun info(message: Any?, t: Throwable?) =
        intercept(LogLevel.INFO, message, t) { delegate.info(message, t) }

    override fun info(message: Any?, t: Throwable?, vararg params: Any?) =
        intercept(LogLevel.INFO, message, t, params) { delegate.info(message, t, *params) }

    override fun debug(message: Any?) =
        intercept(LogLevel.DEBUG, message) { delegate.debug(message) }

    override fun debug(message: Any?, vararg params: Any?) =
        intercept(LogLevel.DEBUG, message, throwable = null, parameters = params) { delegate.debug(message, *params) }

    override fun debug(message: Any?, t: Throwable?) =
        intercept(LogLevel.DEBUG, message, t) { delegate.debug(message, t) }

    override fun debug(message: Any?, t: Throwable?, vararg params: Any?) =
        intercept(LogLevel.DEBUG, message, t, params) { delegate.debug(message, t, *params) }

    override fun trace(message: Any?) =
        intercept(LogLevel.TRACE, message) { delegate.trace(message) }

    override fun trace(message: Any?, vararg params: Any?) =
        intercept(LogLevel.TRACE, message, throwable = null, parameters = params) { delegate.trace(message, *params) }

    override fun trace(message: Any?, t: Throwable?) =
        intercept(LogLevel.TRACE, message, t) { delegate.trace(message, t) }

    override fun trace(message: Any?, t: Throwable?, vararg params: Any?) =
        intercept(LogLevel.TRACE, message, t, params) { delegate.trace(message, t, *params) }

    @Suppress("UNCHECKED_CAST")
    private inline fun intercept(
        level: LogLevel,
        message: Any?,
        throwable: Throwable? = null,
        parameters: Array<*>? = null,
        logging: () -> Unit
    ) {
        val propagateLog = levelInterceptors(level).all { interceptor ->
            val result = when {
                throwable == null && parameters == null -> interceptor.intercept(loggerName, message)
                parameters == null -> interceptor.intercept(loggerName, message, throwable!!)
                throwable == null -> interceptor.intercept(loggerName, message, parameters as Array<Any?>)
                else -> interceptor.intercept(loggerName, message, throwable, parameters as Array<Any?>)
            }
            result == LogPropagation.CONTINUE
        }
        if (propagateLog) logging()
    }
}
