import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask
import org.jetbrains.kotlin.js.backend.ast.JsEmpty.setSource


plugins {
    kotlin("jvm") version "1.8.21"
    java
    application
    idea
    id("com.github.davidmc24.gradle.plugin.avro-base") version "1.7.1"
}

application {
    mainClass.set("org.ruthenia.itc.MainKt")
}

group = "org.ruthenia.itc"
version = "1.0.0"

repositories {
    mavenCentral()
    maven(url = "https://packages.confluent.io/maven")
}

dependencies {
    implementation("org.apache.kafka:kafka-streams:3.4.0")
    implementation("org.apache.kafka:kafka-clients:3.4.0")
    implementation("org.apache.avro:avro:1.10.2")
    implementation("io.confluent:kafka-avro-serializer:6.1.0")
    implementation("io.confluent:kafka-streams-avro-serde:7.4.0")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
    implementation("org.slf4j:slf4j-log4j12:2.0.7")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.0")
}

val generateAvro = tasks.register<GenerateAvroJavaTask>("generateAvro") {
    source("src/main/avro")
    setOutputDir(file("src/main/java/"))
}

//configure<JavaCompile> {
//    this.source = fileTree(generateAvro)
//}


tasks.named("compileJava").configure {
    source(generateAvro)
}

//tasks.withType<GenerateAvroJavaTask> {
//    setOutputDir(file("src/main/kotlin/"))
//}

tasks.test {
    useJUnitPlatform()
}

//tasks.withType<JavaCompile> {
//    this.shouldRunAfter(generateAvro.get())
//}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<Test> {
    useJUnitPlatform()
}