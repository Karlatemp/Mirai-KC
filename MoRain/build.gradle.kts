group = "io.github.karlatemp"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.3.72"
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(project(":mirai-kc"))
    implementation("net.mamoe:mirai-core:1.1-EA")
    implementation("org.ow2.asm:asm-commons:8.0.1")
    implementation("org.ow2.asm:asm-tree:8.0.1")
    implementation("org.jetbrains:annotations:19.0.0")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation(kotlin("stdlib-jdk8", "1.3.72"))
    implementation(kotlinx("coroutines-core", "1.3.4"))
    implementation(kotlinx("coroutines-io", "0.1.16"))
    implementation("org.apache.httpcomponents.client5:httpclient5:5.0.1")
    implementation(rootProject.fileTree("libs").include("*.jar"))
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