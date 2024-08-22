plugins {
    kotlin("jvm") version "2.0.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.paperweight.userdev") version "1.7.2"
}

group = "dev.lrdcxdes"
version = "0.01"

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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
}

val targetJavaVersion = 22
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.assemble {
    dependsOn(tasks.reobfJar)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    destinationDirectory.set(file("C:\\Users\\lrdcxdes\\Desktop\\hardcraft\\plugins"))
}