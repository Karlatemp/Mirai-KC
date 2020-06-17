plugins {
    kotlin("jvm") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "io.github.karlatemp"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("net.mamoe:mirai-core:1.0.0")
    implementation("org.ow2.asm:asm-commons:8.0.1")
    implementation("org.ow2.asm:asm-tree:8.0.1")
    implementation("org.jetbrains:annotations:19.0.0")
    implementation(kotlin("stdlib-jdk8", "1.3.72"))
    implementation(kotlinx("coroutines-core", "1.3.4"))
    implementation(kotlinx("coroutines-io", "0.1.16"))
    implementation("org.apache.httpcomponents:httpclient:4.5.12")
    implementation(fileTree("libs").include("*.jar"))
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

@Suppress("NOTHING_TO_INLINE")
inline fun kotlinx(module: String, version: String? = null): Any =
    "org.jetbrains.kotlinx:kotlinx-$module${version?.let { ":$version" } ?: ""}"

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}