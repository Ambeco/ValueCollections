import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // 2.4.20-Beta1 contains fixes for https://youtrack.jetbrains.com/issue/KT-87474/Kotlin-compiler-error-IndexOutOfBoundsException-Cannot-pop-operand-off-an-empty-stack
    // and https://youtrack.jetbrains.com/issue/KT-87470/Kotlin-compiler-throwing-VerifyError-Operand-stack-underflow
    kotlin("jvm") version "2.4.20-Beta1"
    `java-library`
    `maven-publish`
    id("com.vanniktech.maven.publish") version "0.30.0"
}

// io.github.ambeco is used instead of com.mpd.valuecollections because Maven Central requires proving
// ownership of the group id's domain, and this repo does not control mpd.com; io.github.<user> is
// verified automatically via the GitHub account instead.
group = "io.github.ambeco"
version = (findProperty("libVersion") as String?) ?: "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

dependencies {
    // api, not implementation: androidx.collection types (IntList, IntSet, etc.) appear directly in
    // this library's public function signatures, so consumers need them on their compile classpath too.
    api("androidx.collection:collection:1.4.0")
    api("androidx.collection:collection-ktx:1.4.0")
    api("androidx.collection:collection-jvm:1.6.0")

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

mavenPublishing {
    // Publishes to the Sonatype Central Portal (the only option for namespaces registered since
    // mid-2024). Requires ORG_GRADLE_PROJECT_mavenCentralUsername/Password (a Central Portal user
    // token, not your account password) to be set - see the publish.yml workflow for details.
    // Host must be explicit: this namespace has no legacy OSSRH staging profile, so without it the
    // plugin's default host lookup calls the old Nexus staging API and fails with a 402.
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    // Requires ORG_GRADLE_PROJECT_signingInMemoryKey (ASCII-armored GPG private key) and
    // ORG_GRADLE_PROJECT_signingInMemoryKeyPassword to be set.
    signAllPublications()

    coordinates(group.toString(), "valuecollections", version.toString())

    pom {
        name.set("ValueCollections")
        description.set("Thin, zero(-ish)-overhead wrapper collections around primitive collections, with type-safe converters for Kotlin Int/Long-backed value classes.")
        url.set("https://github.com/Ambeco/ValueCollections")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://github.com/Ambeco/ValueCollections/blob/main/LICENSE")
            }
        }
        developers {
            developer {
                id.set("Ambeco")
                name.set("Ambeco")
                url.set("https://github.com/Ambeco")
            }
        }
        scm {
            url.set("https://github.com/Ambeco/ValueCollections")
            connection.set("scm:git:https://github.com/Ambeco/ValueCollections.git")
            developerConnection.set("scm:git:ssh://git@github.com/Ambeco/ValueCollections.git")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Ambeco/ValueCollections")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}