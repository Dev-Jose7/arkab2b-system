import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.the

plugins {
    id("java")
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.arka"
version = "0.1.0-SNAPSHOT"

description = "ArkaB2B API gateway for external edge security and routing"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.0")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")
    implementation("org.springframework.security:spring-security-oauth2-jose")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.projectreactor:reactor-test")
}

val jacocoReportServices = listOf(
    "identity-access-service",
    "directory-service",
    "catalog-service",
    "inventory-service",
    "order-service",
    "notification-service",
    "reporting-service"
)

val generatedJacocoResourcesDir = layout.buildDirectory.dir("generated-resources/jacoco")

val syncJacocoReports by tasks.registering(Sync::class) {
    into(generatedJacocoResourcesDir)

    jacocoReportServices.forEach { service ->
        from(layout.projectDirectory.dir("../$service/build/reports/jacoco/test/html")) {
            into("static/tools/jacoco/$service")
        }
    }
}

the<SourceSetContainer>().named("main") {
    resources.srcDir(generatedJacocoResourcesDir)
}

tasks.processResources {
    dependsOn(syncJacocoReports)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
