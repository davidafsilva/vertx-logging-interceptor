pluginManagement {
    val kotlinVersion: String by settings
    val axionVersion: String by settings
    val bintrayVersion: String by settings
    val coverallsJacocoVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        id("pl.allegro.tech.build.axion-release") version axionVersion
        id("com.jfrog.bintray") version bintrayVersion
        id("com.github.nbaztec.coveralls-jacoco") version coverallsJacocoVersion
    }
}

rootProject.name = "vertx-logging-interceptor"
