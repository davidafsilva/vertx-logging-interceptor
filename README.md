# Vertx Logging Interceptor 

This small library plugs into Vert.x's logging delegation by leveraging its flexible configuration, allowing 
third-party interceptors to be built and plugged into the application logging mechanism.

## Usage

### Import
#### Gradle
##### Groovy
```groovy
compile "pt.davidafsilva.vertx.logging:vertx-logging-interceptor:VERSION"
```
##### Kotlin DSL
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
- `pt.davidafsilva.vertx.logging.factory.VertxJULLogDelegateFactory` - Java Util Logging 
- `pt.davidafsilva.vertx.logging.factory.VertxSLF4JLogDelegateFactory` - slf4j (1)
- `pt.davidafsilva.vertx.logging.factory.VertxLog4j2LogDelegateFactory` - slf4j2
- `pt.davidafsilva.vertx.logging.factory.VertxLog4jLogDelegateFactory` - log4j

Choose one of the above class names (FQN) according to your runtime logging library and configure it through the 
`vertx.logger-delegate-factory-class-name` system property. You can also set it through a JVM argument: 
`-Dvertx.logger-delegate-factory-class-name=...`.

### Usage
After configuring the appropriate delegate factory, creating and registering an interceptor can be achieved by following 
the steps below:
1. Creating your own Interceptor

1.1. Non-blocking
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
1.2. Blocking 
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
2. Registering your own or built-in interceptors through `LogInterceptors`
```kotlin
LogInterceptors.register(BadWordLogInterceptor) // applicable to all levels
LogInterceptors.register(Level.WARN, ThreadBlockedLogInterceptor()) // built-in interceptor
LogInterceptors.register(Level.INFO, LogMessagesCounterInterceptor()) // counts all info messages
```

| :warning: | There's currently no support for mutating log messages. |
| --- | ---- | 

## Build & Release
At the project root, run the following command:
```shell
./gradlew clean build
```

After a successful build, releasing can be achieve by running:
```shell
./gradlew release publish
```
