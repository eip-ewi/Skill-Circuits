import com.diffplug.gradle.spotless.SpotlessExtension
import nl.javadude.gradle.plugins.license.DownloadLicensesExtension
import nl.javadude.gradle.plugins.license.LicenseExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.configure

val javaVersion = JavaVersion.VERSION_21

plugins {
    `kotlin-dsl` apply false

    java
    idea

    id("com.github.ben-manes.versions").version("0.52.0")
    id("com.diffplug.spotless").version("7.2.1")
    id("com.github.hierynomus.license").version("0.16.1")
}

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

configure<SpotlessExtension> {
    java {
        // Use the eclipse formatter format and import order.
        eclipse().configFile(file("$rootDir/eclipse-formatter.xml"))
        importOrderFile(file("$rootDir/importorder.txt"))

        // Check for a license header in the form of LICENSE.header.java.
        licenseHeaderFile(file("$rootDir/LICENSE.header.java"))

        targetExclude("build/")

        // Default added rules.
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

configure<DownloadLicensesExtension> {
    includeProjectDependencies = true
}

configure<LicenseExtension> {
    header = file("$rootDir/LICENSE.header")
    skipExistingHeaders = false

    mapping(mapOf(
        "java" to "SLASHSTAR_STYLE"
    ))

    excludes(listOf(
        "**/*.json",
        "**/*.css",
        "**/labracore/api/**/*.java"
    ))
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.microsoft.playwright:playwright:1.55.0")
    implementation("com.microsoft.playwright:driver-bundle:1.55.0")

    implementation("org.jetbrains:annotations:26.0.2")

    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.25.1"))
    implementation("org.apache.logging.log4j:log4j-core")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.19.1")

    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.testcontainers:testcontainers:1.21.3")
    testImplementation("org.testcontainers:junit-jupiter:1.21.3")

}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed", "standard_out", "standard_error")
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}
