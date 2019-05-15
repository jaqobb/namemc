## NameMC Api
NameMC Api is a NameMC (https://namemc.com) Java wrapper.

### Add to project
Gradle
```groovy
repositories {
	maven {
		url "https://repository.jaqobb.dev/maven-public/"
	}
}

dependencies {
	implementation "dev.jaqobb:namemc-api:{current version}"
}
```

Gradle Kotlin DSL
```kotlin
repository {
	maven("https://repository.jaqobb.dev/maven-public/")
}

dependencies {
	implementation("dev.jaqobb:namemc-api:{current version}")
}
```

Maven
```xml
<repositories>
	<repository>
		<url>https://repository.jaqobb.dev/maven-public/</url>
	</repository>
</repositories>

<dependencies>
	<dependency>
		<groupId>dev.jaqobb</groupId>
		<artifactId>namemc-api</artifactId>
		<version>{current version}</version>
		<scope>compile</scope>
	</dependency>
</dependencies>
```

### Usage example
Create new `ProfileRepository` object by using:
```java
ProfileRepository profileRepository = new ProfileRepository();
```
or:
```java
ProfileRepository profileRepository = new ProfileRepository(duration, unit);
```

By calling default `ProfileRepository` constructor cached profiles will be valid for 5 minutes.

Create new `ServerRepository` object by using:
```java
ServerRepository serverRepository = new ServerRepository();
```
or:
```java
ServerRepository serverRepository = new ServerRepository(duration, unit);
```

By calling default `ServerRepository` constructor cached servers will be valid for 10 minutes.

Create new `NameMCApi` object by using:
```java
NameMCApi api = new NameMCApi();
```
or:
```java
NameMCApi api = new NameMCApi(profileRepository, serverRepository);
```

By calling default `NameMCApi` constructor default constructors of `ProfileRepository` and `ServerRepository` will be used.

The only method I think you should care about in both repositories is `cache`. This method allows you to cache profile or server (depends on the repository) or if the profile or server is already cached, is valid, and re-cache is not forced, get the profile or server.

I fell like all public methods in `Friend`, `Profile`, and `Server` classes are self explanatory (due to their names) and it is not needed to explain them.
