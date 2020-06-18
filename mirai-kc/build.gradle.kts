import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

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
    // # https://mvnrepository.com/artifact/org.jline/jline
    implementation("org.jline:jline:3.15.0")
    // # https://mvnrepository.com/artifact/org.fusesource.jansi/jansi
    implementation("org.fusesource.jansi:jansi:1.18")
    // # https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("net.mamoe:mirai-core:1.1-EA")
    implementation("net.mamoe:mirai-core-qqandroid:1.1-EA")
    implementation("org.ow2.asm:asm-commons:8.0.1")
    implementation("org.ow2.asm:asm-tree:8.0.1")
    implementation("org.jetbrains:annotations:19.0.0")
    implementation(kotlin("stdlib-jdk8", "1.3.72"))
    implementation(kotlinx("coroutines-core", "1.3.4"))
    implementation(kotlinx("coroutines-io", "0.1.16"))
    implementation("org.apache.httpcomponents:httpclient:4.5.12")
    implementation(rootProject.fileTree("libs").include("*.jar"))
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

@Suppress("NOTHING_TO_INLINE")
inline fun kotlinx(module: String, version: String? = null): Any =
    "org.jetbrains.kotlinx:kotlinx-$module${version?.let { ":$version" } ?: ""}"

tasks {
    compileKotlin {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        kotlinOptions.freeCompilerArgs += "-XXLanguage:+PolymorphicSignature"
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        kotlinOptions.freeCompilerArgs += "-XXLanguage:+PolymorphicSignature"
        kotlinOptions.jvmTarget = "1.8"
    }
}

tasks.withType(ShadowJar::class.java) {
    this.archiveFileName.set("Mirai-KC-${project.version}.jar")
    dependencies {
        exclude {
            when ("${it.moduleGroup}:${it.moduleName}") {
                "net.mamoe:mirai-core" -> true
                "net.mamoe:mirai-core-qqandroid" -> true
                "moe.him188:jcekt" -> true
                "moe.him188:jcekt-common" -> true
                "net.mamoe:mirai" -> true
                "org.bouncycastle:bcprov-jdk15on" -> true
                else -> {
                    when (it.moduleGroup) {
                        "io.ktor" -> true
                        else -> {
                            println("${it.moduleGroup}:${it.moduleName} ${it.moduleVersion}")
                            false
                        }
                    }
                }
            }
        }
    }
    manifest {
        attributes(
            "Manifest-Version" to "1.0",
            "Implementation-Vendor" to "Karlatemp <karlatemp@vip.qq.com>",
            "Implementation-Title" to project.name.toString(),
            "Implementation-Version" to project.version.toString(),
            "Main-Class" to "io.karlatemp.github.mirai.bootstrap.Main"
        )
    }
}
apply(from = project.file("gradle/shadowjar.gradle"))
