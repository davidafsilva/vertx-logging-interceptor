package pt.davidafsilva.vertx.logging.interceptors

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.called
import io.mockk.spyk
import io.mockk.verify
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.Suite
import org.spekframework.spek2.style.specification.describe
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

object ThreadBlockedLogInterceptorTestSpec : Spek({

    describe("a thread blocked log interceptor") {
        context("with the default metric name") {
            testInterception()
        }

        context("with a custom metric name") {
            testInterception("my_custom_metric_name")
        }
    }
})

private fun Suite.testInterception(metricName: String? = null) {
    val expectedMetricName = metricName ?: "vertx_thread_blocked"
    val validLoggerName = "io.vertx.core.impl.BlockedThreadChecker"
    val threadName = "my-awesome-pool-01"
    val validMessageWithDefaultNaming = "Thread Thread[$threadName]=my-custom-task-name " +
        "has been blocked for 123.05 ms, time limit is 2000 ms"

    context("intercepting an invalid logger name") {
        val (registry, interceptor) = createInterceptor(metricName)
        interceptor.intercept("invalid", validMessageWithDefaultNaming)

        it("should not register any metric") {
            verify { registry wasNot called }
        }
    }

    context("intercepting an invalid log message") {
        context("due to its format") {
            val (registry, interceptor) = createInterceptor(metricName)
            interceptor.intercept(validLoggerName, "invalid")

            it("should not register any metric") {
                verify { registry wasNot called }
            }
        }

        context("due to its prefix") {
            val (registry, interceptor) = createInterceptor(metricName)
            interceptor.intercept(
                validLoggerName,
                "Bad Prefix Thread[$threadName]=my-custom-task-name has been blocked for 123.05 ms, time limit is 2000 ms"
            )

            it("should not register any metric") {
                verify { registry wasNot called }
            }
        }

        context("due to its suffix") {
            val (registry, interceptor) = createInterceptor(metricName)
            interceptor.intercept(validLoggerName, "Thread Thread[$threadName]=my-custom-task-name has been.. oops")

            it("should not register any metric") {
                verify { registry wasNot called }
            }
        }
    }

    context("intercepting a valid logger name and thread blocked message") {
        val (registry, interceptor) = createInterceptor(metricName)
        interceptor.intercept(validLoggerName, validMessageWithDefaultNaming)

        val counter = registry.find(expectedMetricName)
            .tag("thread", threadName)
            .counter()
        it("should have registered the expected counter") {
            expectThat(counter).describedAs("counter").isNotNull()
        }

        if (counter != null) {
            val initialCounterValue = counter.count()
            it("should have incremented the counter upon registering it (value=1)") {
                expectThat(initialCounterValue).isEqualTo(1.0)
            }

            interceptor.intercept(validLoggerName, validMessageWithDefaultNaming)
            val secondCounterValue = counter.count()
            it("should increment the counter yet again upon processing another message (value=2)") {
                expectThat(secondCounterValue).isEqualTo(2.0)
            }
        }
    }

    context("intercepting a valid logger name and thread blocked message without the default thread naming") {
        val customThreadName = "CustomThreadName"
        val validMessageWithCustomNaming =
            "Thread $customThreadName=Something has been blocked for 123.05 ms, time limit is 2000 ms"
        val (registry, interceptor) = createInterceptor(metricName)
        interceptor.intercept(validLoggerName, validMessageWithCustomNaming)

        val counter = registry.find(expectedMetricName)
            .tag("thread", customThreadName)
            .counter()
        it("should have registered the expected counter with the expected thread") {
            expectThat(counter).describedAs("counter").isNotNull()
        }
    }

    context("intercepting a valid logger name and thread blocked message with an unexpected thread entry value") {
        val entryStringValue = "weird"
        val validMessageWithCustomNaming =
            "Thread $entryStringValue has been blocked for 123.05 ms, time limit is 2000 ms"
        val (registry, interceptor) = createInterceptor(metricName)
        interceptor.intercept(validLoggerName, validMessageWithCustomNaming)

        val counter = registry.find(expectedMetricName)
            .tag("thread", entryStringValue)
            .counter()
        it("should have registered the expected counter with the whole entry as the thread") {
            expectThat(counter).describedAs("counter").isNotNull()
        }
    }
}

private fun createInterceptor(metricName: String?): Pair<SimpleMeterRegistry, ThreadBlockedLogInterceptor> {
    val registry = spyk<SimpleMeterRegistry>()
    val interceptor = when (metricName) {
        null -> ThreadBlockedLogInterceptor(registry)
        else -> ThreadBlockedLogInterceptor(registry, metricName)
    }
    return Pair(registry, interceptor)
}

