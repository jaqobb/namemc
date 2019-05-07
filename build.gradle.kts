plugins {
	`java-library`
	id("com.github.johnrengelman.shadow") version "5.0.0"
}

group = "dev.jaqobb"
version = "2.0.5"

java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}

defaultTasks("clean", "build", "sourcesJar", "shadowJar")

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
	implementation("org.json:json:20180813")
	testRuntime("org.junit.jupiter:junit-jupiter-engine:5.4.2")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
}
