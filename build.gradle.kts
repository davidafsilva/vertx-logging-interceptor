import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
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
    id("com.github.kt3k.coveralls")
    id("pl.allegro.tech.build.axion-release")
    id("com.jfrog.bintray")
}

group = "pt.davidafsilva.vertx.logging"
scmVersion {
    tag(closureOf<TagNameSerializationConfig> {
        prefix = "v"
        versionSeparator = ""
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

val publicationId = "bintrayPublication" // shared between publish and bintray plugins configuration
val githubRepoId = "davidafsilva/vertx-logging-interceptor"
val githubRepoUrl = "https://github.com/$githubRepoId"
val githubRepoCheckoutUrl = "$githubRepoUrl.git"
val licenseName = "BSD 3-Clause"
val licenseUrl = "https://opensource.org/licenses/BSD-3-Clause"

configure<PublishingExtension> {
    publications {
        create<MavenPublication>(publicationId) {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            pom {
                name.set(project.name)
                description.set(githubRepoUrl)
                url.set(githubRepoUrl)
                inceptionYear.set("2020")
                licenses {
                    license {
                        name.set(licenseName)
                        url.set(licenseUrl)
                        distribution.set("dist")
                    }
                }
                developers {
                    developer {
                        id.set("davidafsilva")
                        name.set("David Silva")
                    }
                }
                scm {
                    connection.set(githubRepoCheckoutUrl)
                    developerConnection.set(githubRepoCheckoutUrl)
                    url.set(githubRepoUrl)
                }
            }
        }
    }
}

configure<BintrayExtension> {
    user = project.findProperty("bintray.user")?.toString() ?: System.getenv("BINTRAY_USER")
    key = project.findProperty("bintray.key")?.toString() ?: System.getenv("BINTRAY_KEY")
    publish = true
    setPublications(publicationId)
    pkg(closureOf<PackageConfig> {
        repo = "maven"
        name = project.name
        desc = "Logging Interceptor Plugin for Vert.x"
        setLabels("kotlin", "vert.x", "logging", "interceptor")
        setLicenses(licenseName)
        websiteUrl = githubRepoUrl
        githubRepo = githubRepoId
        vcsUrl = githubRepoCheckoutUrl
        issueTrackerUrl = "$githubRepoUrl/issues"
        version(closureOf<VersionConfig> {
            name = project.version.toString()
            vcsTag = "v${project.version.toString()}"
        })
    })
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

    withType<JacocoReport> {
        reports {
            xml.isEnabled = true
            html.isEnabled = true
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
    jcenter()
    google()
    maven("https://dl.bintray.com/spekframework/spek")
    maven("https://dl.bintray.com/robfletcher/maven")
    maven("https://dl.bintray.com/christophsturm/maven")
}
