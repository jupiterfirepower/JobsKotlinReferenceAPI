plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.allopen") version "2.1.21"
    id("io.quarkus")
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

val hopliteVersion = "2.9.0"

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-config-yaml")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")

    // postgresql
    implementation("com.github.jasync-sql:jasync-postgresql:2.2.4")
    implementation("net.samyn:kapper:1.4.0")

    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("org.postgresql:postgresql:42.7.5")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion")
    implementation("com.sksamuel.hoplite:hoplite-yaml:$hopliteVersion")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("com.github.f4b6a3:uuid-creator:5.2.0")
    implementation("io.arrow-kt:arrow-core:2.1.2")




    // These are the new dependencies
    //implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.2")
    //implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.11.2")
   // implementation("org.jetbrains.kotlin:kotlin-scripting-jsr223")
   // implementation("org.jetbrains.kotlin:kotlin-script-runtime")

}

group = "org.jobs"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        javaParameters = true
    }
}


