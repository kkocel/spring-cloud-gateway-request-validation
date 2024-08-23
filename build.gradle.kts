import io.gitlab.arturbosch.detekt.getSupportedKotlinVersion

plugins {
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.jmailen.kotlinter") version "4.3.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    val kotlinVersion = "2.0.20"
    id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
}

configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(getSupportedKotlinVersion())
        }
    }
}

dependencyManagement {

    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.3")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.github.microutils:kotlin-logging:3.0.5")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<AbstractCompile> {
    dependsOn(":processResources")
}

tasks.test {
    useJUnitPlatform()
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.BIN
}
