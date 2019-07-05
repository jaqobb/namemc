import com.jfrog.bintray.gradle.BintrayExtension

plugins {
	`java-library`
	`maven-publish`
	id("com.github.johnrengelman.shadow") version "5.1.0"
	id("com.jfrog.bintray") version "1.8.4"
}

group = "dev.jaqobb"
version = "2.0.7"
description = "NameMC (https://namemc.com) Java wrapper"

java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}

defaultTasks("clean", "build", "sourcesJar", "shadowJar", "bintrayUpload")

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
	jcenter()
}

dependencies {
	compileOnly("org.jetbrains:annotations:17.0.0")
	api("org.json:json:20180813")
	testRuntime("org.junit.jupiter:junit-jupiter-engine:5.5.0")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.0")
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = project.group as String
			artifactId = project.name.toLowerCase()
			version = project.version as String
			from(components["java"])
			artifact(tasks["sourcesJar"])
			artifact(tasks["shadowJar"])
		}
	}
}

configure<BintrayExtension> {
	user = properties["bintray-user"] as String?
	key = properties["bintray-api-key"] as String?
	publish = true
	setPublications("maven")
	pkg(closureOf<BintrayExtension.PackageConfig> {
		repo = properties["bintray-repository"] as String?
		name = project.name
		desc = project.description
		websiteUrl = "https://github.com/jaqobb/NameMCAPI"
		issueTrackerUrl = "$websiteUrl/issues"
		vcsUrl = "$websiteUrl.git"
		setLicenses("MIT")
		setLabels("java", "wrapper", "namemc", "minecraft", "api")
	})
}
