import me.champeau.jmh.JMHTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application
    id("me.champeau.jmh") version "0.6.6"
}

group = "edu.sokolov"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    jmh("commons-io:commons-io:2.11.0")
    jmh("org.openjdk.jmh:jmh-core:1.34")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.34")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}

val jarName = "bst-removal-bench.jar"

tasks.named("jmhJar", type = Jar::class) {
    archiveFileName.set(jarName)
}

task("jmhRun", type = JMHTask::class) {
    jarArchive.set(File(File(project.buildDir.absoluteFile, "libs"), jarName))
    resultsFile.set(File(File(project.buildDir.absoluteFile, "results/jmh"), "results.json"))
    resultFormat.set("json")

    failOnError.set(true)

    dependsOn("jmhJar")

}