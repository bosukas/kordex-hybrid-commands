plugins {
    kotlin("jvm") version "1.5.10"
    `maven-publish`
    signing
}

project.group = "io.github.qbosst"
project.version = "1.0.0-SNAPSHOT"
val projectArtifactId = "kordex-hybrid-commands"
val projectGithubUrl = "https://github.com/qbosst/$projectArtifactId"

val releaseRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
val snapshotRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

val kordexVersion = "1.4.2-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven {
        name = "Kotlin Discord"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
}

dependencies {
    compileOnly("com.kotlindiscord.kord.extensions:kord-extensions:$kordexVersion")
    implementation(kotlin("stdlib"))
}

val sourcesJar = task("sourceJar", Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar = task("javadocJar", Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
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
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
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
            name = "Sonatype"
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
