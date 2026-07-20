import gradle.kotlin.dsl.accessors._565523fedfee3a6c98c237d5d815b2e0.allOpen
import gradle.kotlin.dsl.accessors._565523fedfee3a6c98c237d5d815b2e0.java
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.kotlin

plugins {
    id("chat.spring-boot-service")
    id("org.springframework.boot")
    kotlin("plugin.spring")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}