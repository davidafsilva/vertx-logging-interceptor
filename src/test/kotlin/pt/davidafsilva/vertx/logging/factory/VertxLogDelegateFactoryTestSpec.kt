package pt.davidafsilva.vertx.logging.factory

import io.vertx.core.logging.JULLogDelegate
import io.vertx.core.logging.Log4j2LogDelegate
import io.vertx.core.logging.Log4jLogDelegate
import io.vertx.core.logging.SLF4JLogDelegate
import io.vertx.core.spi.logging.LogDelegate
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import kotlin.reflect.KClass

object VertxLogDelegateFactoryTestSpec : Spek({

    data class TestCase(
        val factory: VertxLogDelegateFactory,
        val expectedDelegateType: KClass<out LogDelegate>
    )

    describe("a Vert.x log delegate factory") {
        sequenceOf(
            TestCase(VertxJULLogDelegateFactory(), JULLogDelegate::class),
            TestCase(VertxLog4j2LogDelegateFactory(), Log4j2LogDelegate::class),
            TestCase(VertxLog4jLogDelegateFactory(), Log4jLogDelegate::class),
            TestCase(VertxSLF4JLogDelegateFactory(), SLF4JLogDelegate::class),
        ).forEach { (factory, expectedDelegateType) ->

            val loggerName = "MyLogger"
            val type = factory.javaClass.simpleName
                .removePrefix("Vertx")
                .removeSuffix("LogDelegateFactory")
            context("with type $type") {
                val delegate = factory.createDelegate(loggerName)

                it("should create a intercepted logging delegate") {
                    expectThat(delegate).isA<InterceptedLoggingDelegate>()
                }

                if (delegate is InterceptedLoggingDelegate) {
                    it("should have created a logger with name = $loggerName") {
                        expectThat(delegate.loggerName).isEqualTo(loggerName)
                    }

                    it("should have created a delegate with type = $expectedDelegateType") {
                        expectThat(delegate.delegate::class).isEqualTo(expectedDelegateType)
                    }
                }
            }
        }
    }
})
