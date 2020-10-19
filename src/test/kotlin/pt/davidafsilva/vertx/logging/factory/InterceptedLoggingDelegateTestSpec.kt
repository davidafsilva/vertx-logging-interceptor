package pt.davidafsilva.vertx.logging.factory

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.vertx.core.spi.logging.LogDelegate
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.Suite
import org.spekframework.spek2.style.specification.describe
import pt.davidafsilva.vertx.logging.LogInterceptor
import pt.davidafsilva.vertx.logging.LogLevel
import pt.davidafsilva.vertx.logging.LogPropagation

object InterceptedLoggingDelegateTestSpec : Spek({

    describe("a intercepted logging delegate") {

        context("delegating a FATAL log message") {
            context("with a single message as argument") {
                testInterception(
                    logLevel = LogLevel.ERROR,
                    logDelegateCall = { log, (_, msg) -> log.fatal(msg) },
                    interceptorCall = { interceptor, (loggerName, msg) -> interceptor.intercept(loggerName, msg) }
                )
            }

            context("with single message and a throwable as argument") {
                testInterception(
                    logLevel = LogLevel.ERROR,
                    logDelegateCall = { log, (_, msg, t) -> log.fatal(msg, t) },
                    interceptorCall = { interceptor, (loggerName, msg, t) -> interceptor.intercept(loggerName, msg, t) }
                )
            }
        }

        sequenceOf(
            TestCase(
                logLevel = LogLevel.ERROR,
                messageCall = { log, (_, msg) -> log.error(msg) },
                messageThrowableCall = { log, (_, msg, t) -> log.error(msg, t) },
                messageParametersCall = { log, (_, msg, _, args) -> log.error(msg, *args) },
                messageThrowableParametersCall = { log, (_, msg, t, args) -> log.error(msg, t, *args) }
            ),
            TestCase(
                logLevel = LogLevel.WARN,
                messageCall = { log, (_, msg) -> log.warn(msg) },
                messageThrowableCall = { log, (_, msg, t) -> log.warn(msg, t) },
                messageParametersCall = { log, (_, msg, _, args) -> log.warn(msg, *args) },
                messageThrowableParametersCall = { log, (_, msg, t, args) -> log.warn(msg, t, *args) }
            ),
            TestCase(
                logLevel = LogLevel.INFO,
                messageCall = { log, (_, msg) -> log.info(msg) },
                messageThrowableCall = { log, (_, msg, t) -> log.info(msg, t) },
                messageParametersCall = { log, (_, msg, _, args) -> log.info(msg, *args) },
                messageThrowableParametersCall = { log, (_, msg, t, args) -> log.info(msg, t, *args) }
            ),
            TestCase(
                logLevel = LogLevel.DEBUG,
                messageCall = { log, (_, msg) -> log.debug(msg) },
                messageThrowableCall = { log, (_, msg, t) -> log.debug(msg, t) },
                messageParametersCall = { log, (_, msg, _, args) -> log.debug(msg, *args) },
                messageThrowableParametersCall = { log, (_, msg, t, args) -> log.debug(msg, t, *args) }
            ),
            TestCase(
                logLevel = LogLevel.TRACE,
                messageCall = { log, (_, msg) -> log.trace(msg) },
                messageThrowableCall = { log, (_, msg, t) -> log.trace(msg, t) },
                messageParametersCall = { log, (_, msg, _, args) -> log.trace(msg, *args) },
                messageThrowableParametersCall = { log, (_, msg, t, args) -> log.trace(msg, t, *args) }
            )
        ).forEach { testCase ->
            context("delegating a ${testCase.logLevel} log message") {
                context("with a message as argument") {
                    testInterception(
                        testCase.logLevel,
                        testCase.messageCall,
                        { interceptor, (loggerName, msg) -> interceptor.intercept(loggerName, msg) }
                    )
                }

                context("with a message and throwable as argument") {
                    testInterception(
                        testCase.logLevel,
                        testCase.messageThrowableCall,
                        { interceptor, (loggerName, msg, t) -> interceptor.intercept(loggerName, msg, t) }
                    )
                }

                context("with a message and variable arguments") {
                    testInterception(
                        testCase.logLevel,
                        testCase.messageParametersCall,
                        { interceptor, (loggerName, msg, _, args) -> interceptor.intercept(loggerName, msg, args) }
                    )
                }

                context("with a message, throwable and variable arguments") {
                    testInterception(
                        testCase.logLevel,
                        testCase.messageThrowableParametersCall,
                        { interceptor, (loggerName, msg, t, args) -> interceptor.intercept(loggerName, msg, t, args) }
                    )
                }
            }
        }

        context("delegating null inputs") {
            context("null message") {
                val delegate = mockk<LogDelegate>(relaxed = true)
                val interceptor = logInterceptor("dummy")
                val interceptedDelegate = InterceptedLoggingDelegate("logger", delegate) { listOf(interceptor) }

                interceptedDelegate.error(null)
                it("should call the appropriate interceptor") {
                    verify { interceptor.intercept(any(), isNull()) }
                }

                it("should delegate logging to the underlying logger") {
                    verify { delegate.error(isNull()) }
                }
            }

            context("null throwable") {
                val delegate = mockk<LogDelegate>(relaxed = true)
                val interceptor = logInterceptor("dummy")
                val interceptedDelegate = InterceptedLoggingDelegate("logger", delegate) { listOf(interceptor) }

                interceptedDelegate.error("message", null as Throwable?)
                it("should call the appropriate interceptor") {
                    verify { interceptor.intercept(any(), eq("message")) }
                }

                it("should delegate logging to the underlying logger") {
                    verify { delegate.error(eq("message"), isNull()) }
                }
            }
        }
    }
})

private fun Suite.testInterception(
    logLevel: LogLevel,
    logDelegateCall: (LogDelegate, TestCallInput) -> Unit,
    interceptorCall: (LogInterceptor, TestCallInput) -> Unit,
) {
    val nonBlockedMessage = "regular logging message"
    val blockedMessage = "blocked message"
    val baseInput = TestCallInput(
        loggerName = "someLogger",
        message = "",
        t = RuntimeException(),
        params = arrayOf(123, "test")
    )
    val delegate = mockk<LogDelegate>(relaxed = true)
    val interceptor = logInterceptor(blockedMessage)
    val interceptors = mapOf(
        logLevel to listOf(interceptor)
    )
    val interceptedDelegate = InterceptedLoggingDelegate(baseInput.loggerName, delegate, interceptors::getValue)

    context("being a non-blocked message") {
        val nonBlockedInput = baseInput.copy(message = nonBlockedMessage)
        logDelegateCall(interceptedDelegate, nonBlockedInput)

        it("should call the appropriate interceptor") {
            verify { interceptorCall(interceptor, nonBlockedInput) }
        }

        it("should delegate logging to the underlying logger") {
            verify { logDelegateCall(delegate, nonBlockedInput) }
        }
    }

    context("being a blocked message") {
        val blockedInput = baseInput.copy(message = blockedMessage)
        logDelegateCall(interceptedDelegate, blockedInput)

        it("should call the appropriate interceptor") {
            verify { interceptorCall(interceptor, blockedInput) }
        }

        it("should block the log delegation to the underlying logger") {
            verify(exactly = 0) { logDelegateCall(delegate, blockedInput) }
        }
    }
}

private fun logInterceptor(blockedMessage: String) = mockk<LogInterceptor>().apply {
    every { intercept(any(), any()) } returns LogPropagation.CONTINUE
    every { intercept(any(), eq(blockedMessage)) } returns LogPropagation.BLOCK
    every { intercept(any(), any(), any() as Array<Any?>) } returns LogPropagation.CONTINUE
    every { intercept(any(), eq(blockedMessage), any() as Array<Any?>) } returns LogPropagation.BLOCK
    every { intercept(any(), any(), any<Throwable>()) } returns LogPropagation.CONTINUE
    every { intercept(any(), eq(blockedMessage), any<Throwable>()) } returns LogPropagation.BLOCK
    every { intercept(any(), any(), any(), any()) } returns LogPropagation.CONTINUE
    every { intercept(any(), eq(blockedMessage), any(), any()) } returns LogPropagation.BLOCK
}

@Suppress("ArrayInDataClass")
private data class TestCallInput(val loggerName: String, val message: String, val t: Throwable, val params: Array<Any?>)

private data class TestCase(
    val logLevel: LogLevel,
    val messageCall: (LogDelegate, TestCallInput) -> Unit,
    val messageThrowableCall: (LogDelegate, TestCallInput) -> Unit,
    val messageParametersCall: (LogDelegate, TestCallInput) -> Unit,
    val messageThrowableParametersCall: (LogDelegate, TestCallInput) -> Unit
)
