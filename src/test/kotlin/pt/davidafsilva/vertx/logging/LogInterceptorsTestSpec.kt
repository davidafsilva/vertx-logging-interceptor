package pt.davidafsilva.vertx.logging

import io.mockk.mockk
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.Suite
import org.spekframework.spek2.style.specification.describe
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

object LogInterceptorsTestSpec : Spek({

    describe("A log interceptors centralized storage") {

        executeTestCase("retrieving the log interceptors without any registered interceptor") {
            LogLevel.values().forEach { logLevel ->
                val registeredInterceptors = LogInterceptors.interceptorsFor(logLevel).toList()
                it("should not have any interceptor registered for $logLevel log level") {
                    expectThat(registeredInterceptors).isEmpty()
                }
            }
        }

        executeTestCase("registering a single interceptor for all log levels") {
            val logInterceptor = mockk<LogInterceptor>()
            LogInterceptors.register(logInterceptor)

            LogLevel.values().forEach { logLevel ->
                verifyRegisteredInterceptors(
                    logLevel = logLevel,
                    expectedInOrder = listOf(logInterceptor)
                )
            }
        }

        executeTestCase("register multiple interceptors at once for all log levels") {
            val oneLogInterceptor = mockk<LogInterceptor>()
            val anotherLogInterceptor = mockk<LogInterceptor>()
            LogInterceptors.register(oneLogInterceptor, anotherLogInterceptor)

            LogLevel.values().forEach { logLevel ->
                verifyRegisteredInterceptors(
                    logLevel = logLevel,
                    expectedInOrder = listOf(oneLogInterceptor, anotherLogInterceptor)
                )
            }
        }

        LogLevel.values().forEach { logLevel ->
            executeTestCase("registering an interceptor associated with $logLevel log level") {
                val logInterceptor = mockk<LogInterceptor>()
                LogInterceptors.register(logLevel, logInterceptor)

                verifyRegisteredInterceptors(
                    logLevel = logLevel,
                    expectedInOrder = listOf(logInterceptor)
                )
            }

            executeTestCase("registering multiple interceptors at once associated with $logLevel log level") {
                val oneLogInterceptor = mockk<LogInterceptor>()
                val anotherLogInterceptor = mockk<LogInterceptor>()
                LogInterceptors.register(logLevel, oneLogInterceptor, anotherLogInterceptor)

                verifyRegisteredInterceptors(
                    logLevel = logLevel,
                    expectedInOrder = listOf(oneLogInterceptor, anotherLogInterceptor)
                )
            }
        }
    }
})

private fun Suite.verifyRegisteredInterceptors(
    logLevel: LogLevel,
    expectedInOrder: List<LogInterceptor>
) {
    val registeredInterceptors = LogInterceptors.interceptorsFor(logLevel).toList()
    it("should have exactly the registered interceptor for $logLevel") {
        expectThat(registeredInterceptors).containsExactly(expectedInOrder) // ordered check
    }
}

private fun Suite.executeTestCase(description: String, body: Suite.() -> Unit) = context(description) {
    // fresh start
    LogInterceptors.interceptors.clear()
    // construct the test
    body()
    // fresh exit
    LogInterceptors.interceptors.clear()
}
