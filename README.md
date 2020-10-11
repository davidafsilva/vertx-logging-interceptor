# Vertx Logging Interceptor 

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
compile "pt.davidafsilva.vertx.logging:vertx-logging-interceptor:VERSION"
```
Kotlin DSL:
```kotlin
implementation("pt.davidafsilva.vertx.logging:vertx-logging-interceptor:VERSION")
```
#### Maven
```xml
<dependency>
  <groupId>pt.davidafsilva.vertx.logging</groupId>
  <artifactId>vertx-logging-interceptor</artifactId>
  <version>VERSION</version>
</dependency>
```

### Configuration
Similarly, as Vert.x, it provides 4 possible configurations. One for each supported logging library:
- Java Util Logging : [pt.davidafsilva.vertx.logging.factory.VertxJULLogDelegateFactory](src/main/kotlin/pt/davidafsilva/vertx/logging/factory/VertxLogDelegateFactory.kt#L25)
- Apache Log4j: [pt.davidafsilva.vertx.logging.factory.VertxLog4jLogDelegateFactory](src/main/kotlin/pt/davidafsilva/vertx/logging/factory/VertxLogDelegateFactory.kt#L27)
- Apache Log4j2: [pt.davidafsilva.vertx.logging.factory.VertxLog4j2LogDelegateFactory](src/main/kotlin/pt/davidafsilva/vertx/logging/factory/VertxLogDelegateFactory.kt#L26)
- slf4j: [pt.davidafsilva.vertx.logging.factory.VertxSLF4JLogDelegateFactory](src/main/kotlin/pt/davidafsilva/vertx/logging/factory/VertxLogDelegateFactory.kt#L28)

Choose one of the above class names (FQN) according to your runtime logging library and configure it through the 
`vertx.logger-delegate-factory-class-name` system property. You can set it through a JVM argument:
`-Dvertx.logger-delegate-factory-class-name=pt.davidafsilva.vertx.logging.factory.<chosen delegate factory>`.

### Interceptor Creation
After configuring the appropriate delegate factory, creating an interceptor can be achieved by implementing the 
[LogInterceptor](src/main/kotlin/pt/davidafsilva/vertx/logging/LogInterceptor.kt) interface along with its the required
methods. Optionally, you can opt for [NoOpLogInterceptor](src/main/kotlin/pt/davidafsilva/vertx/logging/LogInterceptor.kt#L12)
which provides an empty skeleton for all the required methods. 

There are essentially two types of interceptors which only differ on their return type for the interception calls.  
Blocking interceptors can be achieved by returning [LogPropagation.BLOCK](src/main/kotlin/pt/davidafsilva/vertx/logging/LogInterceptor.kt#L26)
as the outcome of the interception call. This will effectively stop both the execution of any further existent 
interceptors in the chain alongside with the actual logging of the underlying message.
On the other hand, non-blocking interceptors, which do not affect any sub-sequent operations, can be achieved by 
returning [LogPropagation.CONTINUE](src/main/kotlin/pt/davidafsilva/vertx/logging/LogInterceptor.kt#L26).

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
LogInterceptors.register(Level.WARN, ThreadBlockedLogInterceptor()) // built-in interceptor
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

For the sake of illustration, the metric exported through a prometheus formatted endpoint:
```
# HELP vertx_thread_blocked_total Number of thread blocks detected
# TYPE vertx_thread_blocked_total counter
vertx_thread_blocked_total{thread="vert.x-eventloop-thread-0,5,main",} 1.0
```

## Building and Releasing
At the project root, run the following command:
```shell
./gradlew clean build
```

After a successful build, releasing can be achieved by running:
```shell
./gradlew release publish
```
