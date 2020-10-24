import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import pl.allegro.tech.build.axion.release.domain.ChecksConfig
import pl.allegro.tech.build.axion.release.domain.RepositoryConfig
import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig

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
    jacoco
    id("pl.allegro.tech.build.axion-release") version "1.12.1"
    `maven-publish`
}

group = "pt.davidafsilva.vertx.logging"
scmVersion {
    versionIncrementer(versionIncrementStrategy())
    tag(closureOf<TagNameSerializationConfig> {
        prefix = ""
    })
    checks(closureOf<ChecksConfig> {
        uncommittedChanges = false
    })
    repository(closureOf<RepositoryConfig> {
        pushTagsOnly = true
    })
}
version = scmVersion.version

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

    withType<JacocoCoverageVerification> {
        violationRules {
            rule {
                limit {
                    // jacoco is missing inlined code: https://github.com/jacoco/jacoco/issues/654
                    minimum = 0.8.toBigDecimal()
                }
            }
        }
    }

    val test = findByName("test")!!
    val testCoverage by registering {
        group = "verification"
        description = "Runs both the coverage report and validation"
        dependsOn(":jacocoTestReport", ":jacocoTestCoverageVerification")
    }
    // plugin the test coverage execution after the tests run
    test.finalizedBy(testCoverage)

    withType<Test> {
        useJUnitPlatform {
            includeEngines("spek2")
        }
    }
}

fun versionIncrementStrategy(): String {
    return project.findProperty("release.versionIncrementer")?.toString()
        ?: resolveVersionIncrementStrategyFromLastCommit()
        ?: "incrementMinor"
}

fun resolveVersionIncrementStrategyFromLastCommit(): String? {
    val cmd = "git log -1 --pretty=format:%B"
    val process = Runtime.getRuntime().exec(cmd)
    if (process.waitFor() != 0) return null

    var result: String? = null
    val commitMessage = String(process.inputStream.readBytes())
    if (commitMessage.startsWith('[')) {
        val strategy = commitMessage.substring(1, commitMessage.indexOf(']'))
        result = when (strategy.toLowerCase()) {
            "minor", "patch", "major", "prerelease" -> strategy.toLowerCase().capitalize()
            else -> {
                logger.error("invalid version strategy on commit: $strategy")
                null
            }
        }
    }

    return result
}
