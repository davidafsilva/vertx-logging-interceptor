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
    val vertxVersion: String by project
    implementation(kotlin("stdlib"))
    compileOnly("io.vertx:vertx-core:$vertxVersion")
    compileOnly("io.vertx:vertx-micrometer-metrics:$vertxVersion")

    val spekVersion: String by project
    val kotlinVersion: String by project
    val mockkVersion: String by project
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.vertx:vertx-core:$vertxVersion")
}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "11"
        kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=enable")
    }

    withType<Test> {
        useJUnitPlatform {
            includeEngines("spek2")
        }
    }
}
