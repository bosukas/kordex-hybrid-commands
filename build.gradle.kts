plugins {
    kotlin("jvm") version "1.5.10"
}

group = "com.github.qbosst"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven {
        name = "Kotlin Discord"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
}

dependencies {
    compileOnly("com.kotlindiscord.kord.extensions:kord-extensions:1.4.2-SNAPSHOT")
    implementation(kotlin("stdlib"))
}
