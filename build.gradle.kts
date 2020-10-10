import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

plugins {
    kotlin("jvm") version "1.4.10"
    `maven-publish`
}

group = "pt.davidafsilva.vertx.logging"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib"))

    val vertxVersion: String by project
    compileOnly("io.vertx:vertx-core:$vertxVersion")
    compileOnly("io.vertx:vertx-micrometer-metrics:$vertxVersion")
    val slf4jVersion: String by project
    compileOnly("org.slf4j:slf4j-api:$slf4jVersion")
}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "14"
        kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=enable")
    }
}
