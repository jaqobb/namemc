plugins {
	`java-library`
	`maven-publish`
	id("com.github.johnrengelman.shadow") version "5.1.0"
}

group = "dev.jaqobb"
version = "2.0.5"

java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}

defaultTasks("clean", "build", "sourcesJar", "shadowJar", "publishMavenPublicationToMavenRepository")

tasks {
	test {
		useJUnitPlatform {
			includeEngines("junit-jupiter")
		}
	}
}

task<Jar>("sourcesJar") {
	from(sourceSets["main"].allSource)
	archiveClassifier.set("sources")
}

repositories {
	mavenCentral()
}

dependencies {
	compileOnly("org.jetbrains:annotations:17.0.0")
	api("org.json:json:20180813")
	testRuntime("org.junit.jupiter:junit-jupiter-engine:5.4.2")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = project.group as String
			artifactId = project.name.toLowerCase()
			version = project.version as String
			from(components["java"])
			artifact(tasks["sourcesJar"])
		}
	}
	repositories {
		maven(properties["jaqobb-public-repository-url"] as String) {
			credentials {
				username = properties["jaqobb-repository-user"] as String
				password = properties["jaqobb-repository-password"] as String
			}
		}
	}
}
