plugins {
  this.`java-library`
  this.id("com.github.johnrengelman.shadow") version "5.0.0"
}

group = "dev.jaqobb"
version = "2.0.5"

java {
  this.sourceCompatibility = JavaVersion.VERSION_11
  this.targetCompatibility = JavaVersion.VERSION_11
}

defaultTasks("clean", "build", "sourcesJar", "shadowJar")

tasks {
  test {
    this.useJUnitPlatform {
      this.includeEngines("junit-jupiter")
    }
  }
  shadowJar {
    this.minimize()
  }
}

task<Jar>("sourcesJar") {
  this.from(sourceSets["main"].allSource)
  this.archiveClassifier.set("sources")
}

repositories {
  this.mavenCentral()
}

dependencies {
  this.implementation("org.json:json:20180813")
  this.testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
}