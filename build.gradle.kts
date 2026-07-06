
plugins {
    kotlin("jvm") version "2.4.0"
}

group = "com.mpd.valuecollections"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("androidx.collection:collection:1.4.0")
    implementation("androidx.collection:collection-ktx:1.4.0")
    implementation("androidx.collection:collection-jvm:1.6.0")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
        freeCompilerArgs.add("-Xexplicit-context-parameters")
    }
}