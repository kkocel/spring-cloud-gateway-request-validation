plugins {
    id("org.springframework.boot") version "2.5.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.17.1"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.5.21"
    kotlin("jvm") version "1.5.21"
    kotlin("plugin.spring") version "1.5.21"
}

dependencyManagement {

    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2020.0.3")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

group = "tech.kocel"
project.version = "0.0.1"

repositories {
    maven(url = "https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("io.github.microutils:kotlin-logging:2.0.10")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<AbstractCompile> {
    dependsOn(":processResources")
}

tasks.test {
    useJUnitPlatform()
}

tasks.wrapper {
    gradleVersion = "7.1.1"
    distributionType = Wrapper.DistributionType.BIN
}
