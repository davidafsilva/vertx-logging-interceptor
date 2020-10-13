import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "pt.davidafsilva.vertx.logging"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib"))

    val vertxVersion: String by project
    compileOnly("io.vertx:vertx-core:$vertxVersion")
    compileOnly("io.vertx:vertx-micrometer-metrics:$vertxVersion")
}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "14"
        kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=enable")
    }
}
