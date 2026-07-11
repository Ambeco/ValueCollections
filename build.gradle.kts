import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // 2.4.20-Beta1 contains fixes for https://youtrack.jetbrains.com/issue/KT-87474/Kotlin-compiler-error-IndexOutOfBoundsException-Cannot-pop-operand-off-an-empty-stack
    // and https://youtrack.jetbrains.com/issue/KT-87470/Kotlin-compiler-throwing-VerifyError-Operand-stack-underflow
    kotlin("jvm") version "2.4.20-Beta1"
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
        freeCompilerArgs.add("-Xexplicit-context-parameters")
    }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xcontext-parameters"))
}