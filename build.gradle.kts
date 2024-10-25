plugins {
    kotlin("jvm") version "2.0.21"
    id("com.gradleup.shadow") version "8.3.3"
    id("io.papermc.paperweight.userdev") version "1.7.3"
    kotlin("plugin.serialization") version "2.0.21" // Added for Kotlin serialization
}

group = "dev.lrdcxdes"
version = "0.7.5-alpha"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect")) // Added Kotlin reflection
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8") // Added JDK8 stdlib
    implementation("net.kyori:adventure-api:4.17.0")
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
}

val targetJavaVersion = 21 // Changed to 21 for better compatibility

kotlin {
    jvmToolchain(targetJavaVersion)

    // New compiler options DSL
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(targetJavaVersion)
    }

    shadowJar {
        relocationPrefix = "dev.lrdcxdes.hardcraft"

        archiveClassifier.set("")  // Removed "all" classifier
        destinationDirectory.set(file("C:\\Users\\lrdcxdes\\Desktop\\hardcraft\\plugins"))

        // Relocate Kotlin libraries to avoid conflicts
        relocate("kotlin", "dev.lrdcxdes.hardcraft.lib.kotlin")
        relocate("org.jetbrains", "dev.lrdcxdes.hardcraft.lib.jetbrains")

        // Include all required dependencies
        from(sourceSets.main.get().output)
        configurations = listOf(project.configurations.runtimeClasspath.get())

        // Ensure Kotlin metadata is included
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
        mergeServiceFiles()
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}