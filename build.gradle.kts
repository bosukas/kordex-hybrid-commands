plugins {
    kotlin("jvm") version "1.5.30"
    `maven-publish`
    signing
}

project.group = "io.github.qbosst"
project.version = "1.0.byte-SNAPSHOT"
val projectArtifactId = "kordex-hybrid-commands"
val projectGithubUrl = "https://github.com/qbosst/$projectArtifactId"

val releaseRepoUrl = "https://maven.kotlindiscord.com/repository/community-releases/"
val snapshotRepoUrl = "https://maven.kotlindiscord.com/repository/community-snapshots/"

val kordexVersion = "1.5.byte-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://nexus.zerotwo.bot/repository/m2-snapshots-public/")
}

dependencies {
    compileOnly("com.kotlindiscord.kord.extensions:kord-extensions:$kordexVersion")
    implementation(kotlin("stdlib"))
    testImplementation("com.kotlindiscord.kord.extensions:kord-extensions:$kordexVersion")
    testImplementation("ch.qos.logback:logback-classic:1.2.5")
}

val sourcesJar = task("sourceJar", Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar = task("javadocJar", Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    this.kotlinOptions.jvmTarget = "9"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            artifact(sourcesJar)
            artifact(javadocJar)

            artifactId = projectArtifactId
            groupId = project.group as String
            version = project.version as String

            pom {
                name.set(projectArtifactId)
                description.set("A module for Kord-Extensions that adds hybrid commands.")
                url.set(projectGithubUrl)

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("boss")
                        name.set("qbosst")
                        url.set("https://github.com/qbosst")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/qbosst/${projectArtifactId}.git")
                    developerConnection.set("scm:git:ssh://github.com:qbosst/${projectArtifactId}.git")
                    url.set("$projectGithubUrl/tree/master")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri(if((version as String).endsWith("SNAPSHOT")) snapshotRepoUrl else releaseRepoUrl)

            credentials {
                username = System.getenv("ossrhUsername")
                password = System.getenv("ossrhPassword")
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}
