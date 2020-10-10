package pt.davidafsilva.vertx.logging.factory

import io.vertx.core.logging.JULLogDelegateFactory
import io.vertx.core.logging.Log4j2LogDelegateFactory
import io.vertx.core.logging.Log4jLogDelegateFactory
import io.vertx.core.logging.SLF4JLogDelegateFactory
import io.vertx.core.spi.logging.LogDelegate
import io.vertx.core.spi.logging.LogDelegateFactory
import pt.davidafsilva.vertx.logging.LogInterceptors

sealed class VertxLogDelegateFactory(
    private val delegate: LogDelegateFactory
) : LogDelegateFactory {

    override fun createDelegate(name: String): LogDelegate {
        val original = delegate.createDelegate(name)
        return InterceptedLoggingDelegate(
            loggerName = name,
            delegate = original,
            levelInterceptors = LogInterceptors::interceptorsFor
        )
    }
}

class VertxJULLogDelegateFactory : VertxLogDelegateFactory(JULLogDelegateFactory())
class VertxLog4j2LogDelegateFactory : VertxLogDelegateFactory(Log4j2LogDelegateFactory())
class VertxLog4jLogDelegateFactory : VertxLogDelegateFactory(Log4jLogDelegateFactory())
class VertxSLF4JLogDelegateFactory : VertxLogDelegateFactory(SLF4JLogDelegateFactory())
