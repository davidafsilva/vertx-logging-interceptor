package pt.davidafsilva.vertx.logging

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import strikt.api.expectThat
import strikt.assertions.isEqualTo

object NoOpLogInterceptorTestSpec : Spek({

    describe("an empty, non-blocking default implementation of the LogInterceptor contract") {
        val logInterceptor = object : NoOpLogInterceptor {}

        context("intercepting with a single message as argument") {
            val result = logInterceptor.intercept("any", "any")
            it("should return CONTINUE as its return type") {
                expectThat(result).isEqualTo(LogPropagation.CONTINUE)
            }
        }

        context("intercepting with a message and throwable as argument") {
            val result = logInterceptor.intercept("any", "any", RuntimeException())
            it("should return CONTINUE as its return type") {
                expectThat(result).isEqualTo(LogPropagation.CONTINUE)
            }
        }

        context("intercepting with a message and variable parameters as arguments") {
            val result = logInterceptor.intercept("any", "any", arrayOf("123"))
            it("should return CONTINUE as its return type") {
                expectThat(result).isEqualTo(LogPropagation.CONTINUE)
            }
        }

        context("intercepting with a message, throwable and variable parameters as arguments") {
            val result = logInterceptor.intercept("any", "any", RuntimeException(), arrayOf("123"))
            it("should return CONTINUE as its return type") {
                expectThat(result).isEqualTo(LogPropagation.CONTINUE)
            }
        }
    }
})
