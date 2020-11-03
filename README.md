# Vertx Logging Interceptor 

[![Master Build Status](https://img.shields.io/github/workflow/status/davidafsilva/vertx-logging-interceptor/Master%20Build?label=Build&style=flat-square)](https://github.com/davidafsilva/vertx-logging-interceptor/actions?query=workflow%3A%22Master+Build%22+branch%3Amaster)
[![Coverage Report](https://img.shields.io/coveralls/github/davidafsilva/vertx-logging-interceptor?color=brightgreen&label=Coverage&style=flat-square)](https://coveralls.io/github/davidafsilva/vertx-logging-interceptor)
[![Latest Release](https://img.shields.io/bintray/v/davidafsilva/maven/vertx-logging-interceptor?color=brightgreen&label=Latest%20Relase&style=flat-square)](https://bintray.com/beta/#/davidafsilva/maven/vertx-logging-interceptor)
[![License](https://img.shields.io/github/license/davidafsilva/vertx-logging-interceptor?color=brightgreen&label=License&logo=License&style=flat-square)](https://opensource.org/licenses/BSD-3-Clause)


This small library plugs into Vert.x's logging delegation by leveraging its flexible configuration, allowing 
third-party interceptors to be built and plugged into the application logging mechanism.

## Table of Contents
* [Usage](#usage)
  + [Import](#import)
    - [Gradle](#gradle)
    - [Maven](#maven)
  + [Configuration](#configuration)
  + [Interceptor Creation](#interceptor-creation)
    - [Blocking Interceptors](#blocking-interceptors)
    - [Non-Blocking Interceptors](#non-blocking-interceptors)
  + [Registering Interceptors](#registering-interceptors)
* [Built-In Interceptors](#built-in-interceptors)
  + [ThreadBlockedLogInterceptor](#threadblockedloginterceptor)
* [Building & Releasing](#building---releasing)

## Usage

### Import
#### Gradle
Groovy:
```groovy
compile "pt.davidafsilva.vertx.logging:vertx-logging-interceptor:0.1.0"
```
Kotlin DSL:
```kotlin
implementation("pt.davidafsilva.vertx.logging:vertx-logging-interceptor:0.1.0")
```
#### Maven
```xml
<dependency>
  <groupId>pt.davidafsilva.vertx.logging</groupId>
  <artifactId>vertx-logging-interceptor</artifactId>
  <version>0.1.0</version>
</dependency>
```

### Configuration
Similarly, as Vert.x, it provides 4 possible configurations. One for each supported logging library:

| Factory | Description | 
| --- | --- | 
| [VertxJULLogDelegateFactory](src/main/kotlin/pt/davidafsilva/vertx/logging/factory/VertxLogDelegateFactory.kt#L25) | Creates loggers bound to Java Util Logging | 
| [VertxLog4jLogDelegateFactory](src/main/kotlin/pt/davidafsilva/vertx/logging/factory/VertxLogDelegateFactory.kt#L27) | Creates loggers bound to Apache Log4j (v1) | 
| [VertxLog4j2LogDelegateFactory](src/main/kotlin/pt/davidafsilva/vertx/logging/factory/VertxLogDelegateFactory.kt#L26) | Creates loggers bound to Apache Log4j (v2) | 
| [VertxSLF4JLogDelegateFactory](src/main/kotlin/pt/davidafsilva/vertx/logging/factory/VertxLogDelegateFactory.kt#L28) | Creates loggers bound to the available SLF4J implementation | 

Choose one of the above class names (use FQN) according to your runtime logging library and configure it through the 
`vertx.logger-delegate-factory-class-name` system property. 
Alternatively, you can set it through a JVM argument:
```
-Dvertx.logger-delegate-factory-class-name=pt.davidafsilva.vertx.logging.factory.<chosen delegate factory>
```

### Interceptor Creation
After configuring the appropriate logger factory, creating an interceptor can be achieved by implementing the 
[LogInterceptor](src/main/kotlin/pt/davidafsilva/vertx/logging/LogInterceptor.kt) interface along with the required
methods. Optionally, you can opt for [NoOpLogInterceptor](src/main/kotlin/pt/davidafsilva/vertx/logging/LogInterceptor.kt#L12)
which provides an empty skeleton for all the required methods.

There are essentially two types of interceptors, blocking and non-blocking.  
Blocking interceptors are characterized by returning [LogPropagation.BLOCK](src/main/kotlin/pt/davidafsilva/vertx/logging/LogInterceptor.kt#L26)
as the/a possible outcome of an interception call. These interceptors, upon returning `LogPropagation.BLOCK` will 
effectively stop both the execution of any other interceptors in the chain alongside with the actual logging to the 
underlying logger.  
On the other hand, non-blocking interceptors do not affect any sub-sequent operations. They always return 
[LogPropagation.CONTINUE](src/main/kotlin/pt/davidafsilva/vertx/logging/LogInterceptor.kt#L26).

#### Blocking Interceptors
```kotlin
object LogMessagesCounterInterceptor : NoOpLogInterceptor {
    private val counter = AtomicLong()

    override fun intercept(logger: String, message: Any?): LogPropagation {
        counter.incrementAndGet()
        return LogPropagation.CONTINUE
    }
    
    fun totalLoggedMessages(): Long = counter.get()
}
```

#### Non-Blocking Interceptors 
```kotlin
object BadWordLogInterceptor : NoOpLogInterceptor {
    override fun intercept(logger: String, message: Any?): LogPropagation {
        val strMessage = message.toString()
        return when {
            "badword" in strMessage -> LogPropagation.BLOCK
            else -> LogPropagation.CONTINUE
        }
    }
}
```

| :warning: | There's currently no support for mutating log messages. |
| --- | ---- | 

### Registering Interceptors
Registering your own or built-in interceptors through the centralized `LogInterceptors` storage layer. 
```kotlin
LogInterceptors.register(BadWordLogInterceptor) // applicable to all levels
LogInterceptors.register(Level.WARN, ThreadBlockedLogInterceptor(registry)) // built-in interceptor
LogInterceptors.register(Level.INFO, LogMessagesCounterInterceptor()) // counts all info messages
```



## Built-In Interceptors

### ThreadBlockedLogInterceptor
This interceptor aims to capture thread blocks that are detect by Vert.x 
[BlockedThreadChecker](https://github.com/eclipse-vertx/vert.x/blob/3.9/src/main/java/io/vertx/core/impl/BlockedThreadChecker.java),
providing a metric around it.

The exported metric has the following properties:

| Name | type | Tags | 
| --- | --- | --- | 
| `vertx_thread_blocked_total` | Counter | `thread`: the name of the blocked thread | 

Note that the metric name can be customized by specifying the optional `metricName` constructor parameter.

For the sake of illustration, the metric exported through a prometheus formatted endpoint:
```
# HELP vertx_thread_blocked_total Number of thread blocks detected
# TYPE vertx_thread_blocked_total counter
vertx_thread_blocked_total{thread="vert.x-eventloop-thread-0,5,main",} 1.0
```

## Building 
At the project root, run the following command:
```shell
./gradlew clean build
```

The above command will run both the tests and verification checks.
