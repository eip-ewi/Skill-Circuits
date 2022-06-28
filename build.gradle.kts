import com.diffplug.gradle.spotless.SpotlessExtension
import org.springframework.boot.gradle.tasks.bundling.BootJar
import nl.javadude.gradle.plugins.license.DownloadLicensesExtension
import nl.javadude.gradle.plugins.license.LicenseExtension

group = "nl.tudelft.skills"
version = "2.0.0"

val javaVersion = JavaVersion.VERSION_17

val labradoorVersion = "1.2.0"
val libradorVersion = "1.0.3-SNAPSHOT7"

val genSourceDir = file("$buildDir/skills/src/main/java")

ext["log4j2.version"] = "2.16.0"

repositories {
    mavenLocal()
    mavenCentral()

    maven {
        url = uri("https://artifacts.alfresco.com/nexus/content/repositories/public")
    }
    maven {
        url = uri("https://build.shibboleth.net/nexus/content/repositories/releases")
    }
    maven {
        url = uri("https://gitlab.ewi.tudelft.nl/api/v4/projects/3611/packages/maven")
    }
    maven {
        url = uri("https://gitlab.ewi.tudelft.nl/api/v4/projects/3634/packages/maven")
    }
}

plugins {
    `kotlin-dsl` apply false

    java
    idea
    jacoco
    `maven-publish`

    id("org.springframework.boot").version("2.5.6")
    id("io.spring.dependency-management").version("1.0.11.RELEASE")
    id("com.github.ben-manes.versions").version("0.39.0")

    id("com.diffplug.spotless").version("6.0.1")

    id("com.github.hierynomus.license").version("0.16.1")
}

sourceSets {
    main {
        java {
            srcDir(file("src/main/java"))
            srcDir(genSourceDir)
        }
    }

    test {}
}

configurations {
    developmentOnly
    runtimeClasspath {
        extendsFrom(configurations.developmentOnly.get())
    }
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
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

configure<SpotlessExtension> {
    java {
        // Use the eclipse formatter format and import order.
        eclipse().configFile(file("eclipse-formatter.xml"))
        importOrderFile(file("$rootDir/importorder.txt"))

        // Check for a license header in the form of LICENSE.header.java.
        licenseHeaderFile(file("$rootDir/LICENSE.header.java"))

        targetExclude("build/")

        // Default added rules.
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("html") {
        target("src/main/resources/**/*.html", "src/main/resources/**/*.js", "src/main/resources/scss/**/*.scss")
        prettier("2.6").config(mapOf(
            "tabWidth" to 4, "semi" to true,
            "printWidth" to 100,
            "bracketSameLine" to true,
            "arrowParens" to "avoid",
            "htmlWhitespaceSensitivity" to "ignore"))
    }
}

val jacocoTestReport by tasks.getting(JacocoReport::class) {
    group = "Reporting"

    classDirectories.setFrom(files(classDirectories.files.flatMap {
        val f = files(fileTree(it).exclude("nl/tudelft/skills/dto/id", "nl/tudelft/skills/config"))
        f.filter { file -> !file.name.startsWith("DevDatabaseLoader")  }
    }))

    reports {
        xml.required.set(true)
        csv.required.set(true)

        html.destination = file("$buildDir/reports/coverage")
    }
}

task<Exec>("sassCompile") {
    commandLine("sass", "src/main/resources/scss:src/main/resources/static/css")
}

val processResources by tasks.getting(ProcessResources::class) {
    dependsOn.add(tasks.getByName("sassCompile"))
}

val bootJar by tasks.getting(BootJar::class) {
    enabled = true
}
tasks.getByName<Jar>("jar") {
    enabled = false
}

publishing {
    publications {
        create<MavenPublication>("generatePom") {
            from(components.findByName("java"))
            pom {
                withXml {
                    val repos = asNode().appendNode("repositories")
                    fun repository(id: String, url: String) {
                        val repo = repos.appendNode("repository")
                        repo.appendNode("id", id)
                        repo.appendNode("url", url)
                    }

                    repository("maven-central", "https://repo.maven.apache.org/maven2")
                    repository("shibboleth", "https://build.shibboleth.net/nexus/content/repositories/releases")
                    repository("librador", "https://gitlab.ewi.tudelft.nl/api/v4/projects/3634/packages/maven")
                    repository("labradoor", "https://gitlab.ewi.tudelft.nl/api/v4/projects/3611/packages/maven")
                }
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    minHeapSize = "256m"
    maxHeapSize = "1024m"
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

dependencies {
    // Labrador
    implementation("nl.tudelft.labrador:labradoor:$labradoorVersion") {
        exclude("org.springframework.boot", "spring-boot-devtools")
    }
    implementation("nl.tudelft.labrador:librador:$libradorVersion") {
        exclude("org.springframework.boot", "spring-boot-devtools")
    }

    // SAML
    implementation("com.github.ulisesbocchio:spring-boot-security-saml:1.17")

    // Hibernate
    implementation("org.hibernate:hibernate-core")
    implementation("org.hibernate:hibernate-java8")

    // DB Drivers / Migration
    implementation("org.liquibase:liquibase-core")
    implementation("com.h2database:h2")
    implementation("mysql:mysql-connector-java")
    implementation("org.mariadb.jdbc:mariadb-java-client")
    implementation("org.postgresql:postgresql")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-rest")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-tomcat")
    implementation("org.springframework:spring-web")
    implementation("org.springframework.security:spring-security-messaging")

    // Spring Boot security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Thymeleaf layout dialect
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect")

    // Guava
    implementation("com.google.guava:guava:31.0.1-jre")

    // Modemapper
    implementation("org.modelmapper:modelmapper:2.4.5")

    // Webjars
    implementation("org.webjars:webjars-locator-core")
    implementation("org.webjars:jquery:3.6.0")
    implementation("org.webjars:js-cookie:2.2.1")
    // Better DateTime handling in JavaScript
    implementation("org.webjars.npm:luxon:2.3.2")

    //// Websockets
    //implementation("org.webjars:sockjs-client:1.5.1")
    //implementation("org.webjars:stomp-websocket:2.3.4")
    implementation("org.webjars:font-awesome:6.0.0")
    implementation("org.webjars:chartjs:3.6.0")

    // Jaeger
    implementation("io.opentracing.contrib:opentracing-spring-jaeger-starter:3.3.1")
    implementation("io.opentracing.contrib:opentracing-spring-jaeger-web-starter:3.3.1")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("junit", "junit")
        exclude("org.junit.vintage", "junit-vintage-engine")
    }
    testImplementation("org.springframework.security:spring-security-test") {
        exclude("junit", "junit")
        exclude("org.junit.vintage", "junit-vintage-engine")
    }

    testImplementation("org.junit.jupiter:junit-jupiter")
    runtimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("org.apache.httpcomponents:httpclient")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // Sentry
    implementation("io.sentry:sentry-spring-boot-starter:5.6.0")
    implementation("io.sentry:sentry-logback:5.6.0")

}