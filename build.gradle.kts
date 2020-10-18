import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/spekframework/spek")
    maven("https://dl.bintray.com/robfletcher/maven")
    maven("https://dl.bintray.com/christophsturm/maven")
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
    val striktVersion: String by project
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testImplementation("io.vertx:vertx-core:$vertxVersion")
    testImplementation("io.vertx:vertx-micrometer-metrics:$vertxVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.strikt:strikt-core:$striktVersion")

    // logging implementations for testing
    testImplementation("org.slf4j:slf4j-api:1.7.30") // slf4j
    testImplementation("log4j:log4j:1.2.17") // log4j 1.x
    testImplementation("org.apache.logging.log4j:log4j-core:2.13.3") // log4j 2.x
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
