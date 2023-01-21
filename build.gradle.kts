plugins {
    // https://github.com/spring-gradle-plugins/dependency-management-plugin
    id("io.spring.dependency-management") version "1.1.0"

    // https://docs.spring.io/spring-boot/docs/3.0.x/gradle-plugin/reference/htmlsingle/
    id("org.springframework.boot") version "3.0.2"

    // https://github.com/n0mer/gradle-git-properties
    id("com.gorylenko.gradle-git-properties") version "2.4.1"

    // https://github.com/spotbugs/spotbugs-gradle-plugin
    id("com.github.spotbugs") version "5.0.13"

    // https://github.com/spring-io/spring-javaformat
    id("io.spring.javaformat") version "0.0.35"

    // https://github.com/ben-manes/gradle-versions-plugin
    id("com.github.ben-manes.versions") version "0.44.0"

    checkstyle
    jacoco
    java
    idea
}

group = "se.urvantsev"
version = "0.0.1-SNAPSHOT"
description = "Recruitment Test"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    checkstyle("io.spring.javaformat:spring-javaformat-checkstyle:0.0.35")

    compileOnly("com.github.spotbugs:spotbugs-annotations:4.7.3")
    testCompileOnly("com.github.spotbugs:spotbugs-annotations:4.7.3")
    // https://github.com/KengoTODA/findbugs-slf4j
    spotbugsPlugins("jp.skypencil.findbugs.slf4j:bug-pattern:1.5.0@jar")
    // https://github.com/mebigfatguy/fb-contrib/releases/latest
    spotbugsPlugins("com.mebigfatguy.sb-contrib:sb-contrib:7.4.7")
    // https://github.com/find-sec-bugs/find-sec-bugs/
    spotbugsPlugins("com.h3xstream.findsecbugs:findsecbugs-plugin:1.12.0")

    implementation("com.formdev:flatlaf:3.0")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("com.squareup.okhttp3:okhttp")

    //testRuntimeOnly("org.junit.platform:junit-platform-launcher:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    // https://assertj.github.io/doc/#assertj-overview
    testImplementation("org.assertj:assertj-core:3.24.2")

    // https://github.com/datafaker-net/datafaker/
    testImplementation("net.datafaker:datafaker:1.7.0")
}

tasks.processResources {
    val tokens = mapOf(
            "application.version" to project.version,
            "application.description" to project.description
    )
    filesMatching("**/*.yml") {
        filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokens)
    }
}

checkstyle {
    toolVersion = "10.5.0"
}

tasks.test {
    failFast = false
    enableAssertions = true
    useJUnitPlatform()

    testLogging {
        events("PASSED", "STARTED", "FAILED", "SKIPPED")
        // Set to true if you want to see output from tests
        showStandardStreams = false
        setExceptionFormat("FULL")
    }

    // report is always generated after tests run
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    // tests are required to run before generating the report
    dependsOn(tasks.test)
    reports {
        csv.required.set(true)
        xml.required.set(true)
    }
}

spotbugs {
    // https://github.com/spotbugs/spotbugs/releases/latest
    toolVersion.set("4.7.3")
    excludeFilter.set(file("findbugs-exclude.xml"))
}

tasks {
    spotbugsMain {
        ignoreFailures = true
        effort.set(com.github.spotbugs.snom.Effort.MAX)
        reportLevel.set(com.github.spotbugs.snom.Confidence.LOW)
        reports.create("html") {
            enabled = true
        }
    }
    spotbugsTest {
        ignoreFailures = true
        effort.set(com.github.spotbugs.snom.Effort.MIN)
        reportLevel.set(com.github.spotbugs.snom.Confidence.HIGH)
        reports.create("html") {
            enabled = true
        }
    }
}

defaultTasks("format", "build")
