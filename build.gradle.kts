plugins {
    // https://github.com/spring-gradle-plugins/dependency-management-plugin
    id("io.spring.dependency-management") version "1.1.4"

    // https://docs.spring.io/spring-boot/docs/3.2.x/gradle-plugin/reference/htmlsingle/
    id("org.springframework.boot") version "3.2.1"

    // https://github.com/n0mer/gradle-git-properties
    id("com.gorylenko.gradle-git-properties") version "2.4.1"

    // https://github.com/spotbugs/spotbugs-gradle-plugin
    id("com.github.spotbugs") version "6.0.6"

    // https://github.com/diffplug/spotless/tree/main/plugin-gradle
    id("com.diffplug.spotless") version "6.23.3"

    // https://github.com/ben-manes/gradle-versions-plugin
    id("com.github.ben-manes.versions") version "0.48.0"

    // https://github.com/graalvm/native-build-tools
    // https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html
    id("org.graalvm.buildtools.native") version "0.9.28"

    checkstyle
    jacoco
    java
    idea
}

val String.v: String get() = rootProject.extra["$this.version"] as String


group = "se.urvantsev"
version = "0.0.1-SNAPSHOT"
description = "Recruitment Test"

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        // https://github.com/spotbugs/spotbugs/issues/2567
        mavenBom("org.ow2.asm:asm-bom:9.6")
    }
}

dependencies {
    checkstyle("com.puppycrawl.tools:checkstyle:${"checkstyle".v}")

    compileOnly("com.github.spotbugs:spotbugs-annotations:${"spotbugs".v}")
    testCompileOnly("com.github.spotbugs:spotbugs-annotations:${"spotbugs".v}")
    // https://github.com/KengoTODA/findbugs-slf4j
    spotbugsPlugins("jp.skypencil.findbugs.slf4j:bug-pattern:1.5.0@jar")
    // https://github.com/mebigfatguy/fb-contrib/releases/latest
    spotbugsPlugins("com.mebigfatguy.sb-contrib:sb-contrib:7.6.4")
    // https://github.com/find-sec-bugs/find-sec-bugs/
    spotbugsPlugins("com.h3xstream.findsecbugs:findsecbugs-plugin:1.12.0")

    // https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#configuration-metadata-annotation-processor
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    // https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#beans-scanning-index
    annotationProcessor("org.springframework:spring-context-indexer")

    implementation("com.formdev:flatlaf:3.2.5")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("com.squareup.okhttp3:okhttp")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    // https://assertj.github.io/doc/#assertj-overview
    testImplementation("org.assertj:assertj-core:3.25.1")

    // https://github.com/datafaker-net/datafaker/
    testImplementation("net.datafaker:datafaker:2.1.0")
}

spotless {
    java {
        target("src/main/java", "src/test/java")
        // https://github.com/palantir/palantir-java-format/releases
        palantirJavaFormat("2.39.0")
    }
}

checkstyle {
    toolVersion = "checkstyle".v
    configDirectory.set(File(rootDir, "src/main/checkstyle"))
}

spotbugs {
    // https://github.com/spotbugs/spotbugs/releases/latest
    toolVersion.set("spotbugs".v)
    // By default, spotbugs verifies TEST classes as well, and we do not want that
    excludeFilter.set(file("findbugs-exclude.xml"))
}



tasks {
    processResources {
        val tokens = mapOf(
            "application.version" to project.version,
            "application.description" to project.description
        )
        filesMatching("**/*.yml") {
            filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokens)
        }
    }
    checkstyleMain {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
    checkstyleAot {
        enabled = false
    }
    checkstyleTest {
        enabled = false
    }

    compileJava {
        dependsOn("processResources")
    }

    test {
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
        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        // tests are required to run before generating the report
        dependsOn(test)
        reports {
            csv.required.set(true)
            xml.required.set(true)
        }
    }

    spotbugsMain {
        ignoreFailures = true
        effort.set(com.github.spotbugs.snom.Effort.MAX)
        reportLevel.set(com.github.spotbugs.snom.Confidence.LOW)
        reports.create("html") {
            enabled = true
        }
    }
    spotbugsAot {
        enabled = false
    }
    spotbugsAotTest {
        enabled = false
    }
    spotbugsTest {
        enabled = false
    }

    bootRun {
        jvmArgs("-Djava.awt.headless=false")
    }
}

graalvmNative {
    metadataRepository {
        enabled.set(true)
        // https://github.com/oracle/graalvm-reachability-metadata/releases
        version.set("0.3.5")
    }
    binaries {
        named("main") {
            resources.autodetect()
            fallback.set(false)
            richOutput.set(true)
            buildArgs.add("-Djava.awt.headless=false")
        }
    }
}



defaultTasks("spotlessApply", "build")
