package pt.davidafsilva.vertx.logging.interceptors

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.vertx.micrometer.backends.BackendRegistries
import pt.davidafsilva.vertx.logging.LogPropagation
import pt.davidafsilva.vertx.logging.NoOpLogInterceptor

class ThreadBlockedLogInterceptor @JvmOverloads constructor(
    private val registry: MeterRegistry = BackendRegistries.getDefaultNow(),
    private val metricName: String = VERTX_THREAD_BLOCKED_COUNTER_NAME
) : NoOpLogInterceptor {

    companion object {
        private val LOGGER_NAME = io.vertx.core.impl.BlockedThreadChecker::class.java.canonicalName
        private const val TEMPLATE = "Thread E has been blocked for M ms, time limit is L ms"
        private const val MIN_LENGTH = TEMPLATE.length
        private const val MESSAGE_PREFIX_INDEX = 0
        private const val MESSAGE_PREFIX = "Thread"
        private const val MESSAGE_BLOCKED_THREAD_ENTRY_INDEX = 1
        private const val MESSAGE_BLOCKED_THREAD_ENTRY_THREAD_PREFIX = "Thread["
        private const val MESSAGE_BLOCKED_THREAD_ENTRY_THREAD_SUFFIX = "]"
        private const val MESSAGE_BLOCKED_THREAD_ENTRY_DELIMITER = "="
        private const val MESSAGE_BLOCKED_SUB_STR_INDEX = 2
        private const val MESSAGE_BLOCKED_SUB_STR = "has been blocked for"

        private const val VERTX_THREAD_BLOCKED_COUNTER_NAME = "vertx_thread_blocked"
        private const val VERTX_THREAD_BLOCKED_COUNTER_DESC = "Number of thread blocks detected"
        private const val THREAD_TAG = "thread"
    }

    override fun intercept(logger: String, message: Any?): LogPropagation {
        if (logger == LOGGER_NAME) {
            getBlockedThread(message.toString())?.let(::incrementThreadBlockedCounter)
        }
        return LogPropagation.CONTINUE // don't block the logging
    }

    private fun incrementThreadBlockedCounter(blockedThread: String) = Counter
        .builder(metricName)
        .description(VERTX_THREAD_BLOCKED_COUNTER_DESC)
        .tag(THREAD_TAG, blockedThread)
        .register(registry)
        .increment()

    private fun getBlockedThread(message: String): String? {
        if (message.length < MIN_LENGTH) return null

        val parts = message.split(" ", limit = 3)
        val valid = parts.size == 3 &&
            parts[MESSAGE_PREFIX_INDEX] == MESSAGE_PREFIX &&
            parts[MESSAGE_BLOCKED_SUB_STR_INDEX].startsWith(MESSAGE_BLOCKED_SUB_STR)

        return if (valid) getThreadNameFromEntry(parts[MESSAGE_BLOCKED_THREAD_ENTRY_INDEX]) else null
    }

    // entry is formatted through AbstractMap.SimpleEntry toString
    private fun getThreadNameFromEntry(entry: String): String = when {
        // default format: Thread[<thread_info_here>]=<task>
        entry.startsWith(MESSAGE_BLOCKED_THREAD_ENTRY_THREAD_PREFIX) -> {
            // index of ]=
            val entryDelimiter =
                entry.indexOf("$MESSAGE_BLOCKED_THREAD_ENTRY_THREAD_SUFFIX$MESSAGE_BLOCKED_THREAD_ENTRY_DELIMITER")
            if (entryDelimiter < 0) entry
            else entry.substring(
                // remove Thread[
                MESSAGE_BLOCKED_THREAD_ENTRY_THREAD_PREFIX.length,
                // up to ]=
                entryDelimiter
            )
        }
        // probably a custom thread formatting was defined
        else -> {
            // might need adjusting for threads with '=' chars within its toString()
            val entryDelimiter = entry.indexOf(MESSAGE_BLOCKED_THREAD_ENTRY_DELIMITER)
            if (entryDelimiter >= 0) entry.substring(0, entryDelimiter)
            else entry
        }
    }
}
