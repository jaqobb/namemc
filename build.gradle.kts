plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.jaqobb"
version = "3.0.1"
description = "Lightweight, straightforward, easy-to-use and fast NameMC Java wrapper"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.googlecode.json-simple:json-simple:1.1.1") {
        exclude(group = "junit", module = "junit")
    }
    testImplementation(platform("org.junit:junit-bom:5.11.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.awaitility:awaitility:4.2.2")
}

tasks.shadowJar {
    relocate("org.json.simple", "dev.jaqobb.namemc.library.org.json.simple")
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

publishing {
    repositories {
        maven {
            name = if (!project.version.toString().endsWith("SNAPSHOT")) "jaqobbRepositoryReleases" else "jaqobbRepositorySnapshots"
            url = if (!project.version.toString().endsWith("SNAPSHOT")) uri("https://repository.jaqobb.dev/releases") else uri("https://repository.jaqobb.dev/snapshots")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])
            artifact(tasks["sourcesJar"])
        }
    }
}
