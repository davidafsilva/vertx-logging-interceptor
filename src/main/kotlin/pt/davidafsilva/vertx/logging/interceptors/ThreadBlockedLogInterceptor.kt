package pt.davidafsilva.vertx.logging.interceptors

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.vertx.micrometer.backends.BackendRegistries
import pt.davidafsilva.vertx.logging.LogPropagation
import pt.davidafsilva.vertx.logging.NoOpLogInterceptor

class ThreadBlockedLogInterceptor @JvmOverloads constructor(
    private val registry: MeterRegistry = BackendRegistries.getDefaultNow()
) : NoOpLogInterceptor {

    companion object {
        private val LOGGER_NAME = io.vertx.core.impl.BlockedThreadChecker::class.java.canonicalName
        private const val TEMPLATE = "Thread T has been blocked for M ms, time limit is L ms"
        private const val MIN_LENGTH = TEMPLATE.length
        private const val MESSAGE_PREFIX_INDEX = 0
        private const val MESSAGE_PREFIX = "Thread"
        private const val MESSAGE_BLOCKED_SUB_STR_INDEX = 2
        private const val MESSAGE_BLOCKED_SUB_STR = "has been blocked for"

        private const val VERTX_THREAD_BLOCKED_COUNTER_NAME = "vertx_thread_blocked"
        private const val THREAD_TAG = "thread"
    }

    override fun intercept(logger: String, message: Any?): LogPropagation {
        if (logger == LOGGER_NAME) {
            getBlockedThread(message.toString())?.let(::incrementThreadBlockedCounter)
        }
        return LogPropagation.CONTINUE // don't block the logging
    }

    private fun incrementThreadBlockedCounter(blockedThread: String) = registry.counter(
        VERTX_THREAD_BLOCKED_COUNTER_NAME,
        listOf(Tag.of(THREAD_TAG, blockedThread))
    ).increment()

    private fun getBlockedThread(message: String): String? {
        if (message.length < MIN_LENGTH) return null

        val parts = message.split(" ", limit = 3)
        val valid = parts.size == 3 &&
            parts[MESSAGE_PREFIX_INDEX] == MESSAGE_PREFIX &&
            parts[MESSAGE_BLOCKED_SUB_STR_INDEX].startsWith(MESSAGE_BLOCKED_SUB_STR)
        return if (valid) parts[1] else null
    }
}
