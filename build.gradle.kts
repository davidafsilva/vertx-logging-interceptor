import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.kt3k.gradle.plugin.CoverallsPluginExtension
import pl.allegro.tech.build.axion.release.domain.ChecksConfig
import pl.allegro.tech.build.axion.release.domain.RepositoryConfig
import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig

// register repositories for both buildscript and application
buildscript.repositories.registerRepositories()
repositories.registerRepositories()

plugins {
    kotlin("jvm")
    jacoco
    `maven-publish`
    signing
    id("com.github.kt3k.coveralls")
    id("pl.allegro.tech.build.axion-release")
}

group = "pt.davidafsilva.vertx"
scmVersion {
    tag(closureOf<TagNameSerializationConfig> {
        prefix = "v"
        versionSeparator = ""
    })
    checks(closureOf<ChecksConfig> {
        isUncommittedChanges = false
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
    val slf4jVersion: String by project
    val log4jVersion: String by project
    val log4j2Version: String by project
    testImplementation("org.slf4j:slf4j-api:$slf4jVersion") // slf4j
    testImplementation("log4j:log4j:$log4jVersion") // log4j 1.x
    testImplementation("org.apache.logging.log4j:log4j-core:$log4j2Version") // log4j 2.x
}

configure<JavaPluginExtension> {
    withSourcesJar()
    withJavadocJar()
}

configure<PublishingExtension> {
    repositories {
        maven {
            name = "Sonatype"
            val repository = when {
                version.toString().endsWith("-SNAPSHOT") -> "/content/repositories/snapshots/"
                else -> "/service/local/staging/deploy/maven2/"
            }
            setUrl("https://s01.oss.sonatype.org/$repository")
            credentials {
                username = System.getenv("OSSRH_USER")
                password = System.getenv("OSSRH_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("artifacts") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            pom {
                val githubRepoUrl = "https://github.com/davidafsilva/vertx-logging-interceptor"

                name.set(project.name)
                description.set("https://github.com/davidafsilva/vertx-logging-interceptor")
                url.set("https://github.com/davidafsilva/vertx-logging-interceptor")
                inceptionYear.set("2020")
                licenses {
                    license {
                        name.set("BSD 3-Clause")
                        url.set("https://opensource.org/licenses/BSD-3-Clause")
                    }
                }
                developers {
                    developer {
                        id.set("davidafsilva")
                        name.set("David Silva")
                        url.set("https://github.com/davidafsilva")
                    }
                }
                scm {
                    val githubRepoCheckoutUrl = "$githubRepoUrl.git"

                    connection.set(githubRepoCheckoutUrl)
                    developerConnection.set(githubRepoCheckoutUrl)
                    url.set(githubRepoUrl)
                }
            }
        }
    }
}

configure<SigningExtension> {
    val signingGpgKey: String? by project
    val signingGpgKeyId: String? by project
    val signingGpgKeyPassword: String? by project
    if (signingGpgKey != null && signingGpgKeyId != null && signingGpgKeyPassword != null) {
        useInMemoryPgpKeys(signingGpgKeyId, signingGpgKey, signingGpgKeyPassword)
    }

    sign(publishing.publications.getByName("artifacts"))
}

configure<CoverallsPluginExtension> {
    sourceDirs = sourceDirs + "src/main/kotlin"
}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=all")
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

    withType<JacocoReport> {
        reports {
            xml.required.set(true)
            html.required.set(true)
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

fun RepositoryHandler.registerRepositories() {
    mavenLocal()
    mavenCentral()
}
