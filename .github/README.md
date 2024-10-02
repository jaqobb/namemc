# NameMC

Lightweight, straightforward, easy-to-use and fast NameMC Java wrapper. This Java wrapper utilizes endpoints exposed by NameMC to provide you with the ability to get information about profiles and servers. Unfortunately, NameMC, to my knowledge, does not offer a lot of endpoints, so this wrapper is limited in terms of functionality. If you come across an endpoint that is not used by the wrapper, feel free to open an issue or a pull request.

**This wrapper is not affiliated with NameMC.**

## Adding to your project

### Maven
```xml
<repositories>
    <repository>
        <id>jaqobb-repository-releases</id>
        <name>jaqobb Repository</name>
        <url>https://repository.jaqobb.dev/releases</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>dev.jaqobb</groupId>
        <artifactId>namemc</artifactId>
        <version>3.0.1</version>
    </dependency>
</dependencies>
```

### Gradle (Kotlin)
```kotlin
repositories {
    maven {
        name = "jaqobbRepositoryReleases"
        url = uri("https://repository.jaqobb.dev/releases")
    }
}

dependencies {
    implementation("dev.jaqobb:namemc:3.0.1")   
}
```

### Gradle (Groovy)
```groovy
repositories {
    maven {
        name "jaqobbRepositoryReleases"
        url "https://repository.jaqobb.dev/releases"
    }
}

dependencies {
    implementation "dev.jaqobb:namemc:3.0.1"
}
```

## Usage

If you intend to use both the profile and server repositories, you can use the `NameMC` class to get instances of both repositories. Otherwise, you can create a new instance of the repository you need, either `ProfileRepository` or `ServerRepository`.

```java
NameMC nameMC = new NameMC();
ProfileRepository profileRepository = nameMC.getProfileRepository();
ServerRepository serverRepository = nameMC.getServerRepository();
```

```java
ProfileRepository profileRepository = new ProfileRepository();
ServerRepository serverRepository = new ServerRepository();
```

You can also use other available constructors to create instances of the repositories with modified cache settings. By default, the repositories use a cache with a time-to-live of 30 minutes and infinite maximum cache size (to mimic versions prior to the update introducing modifying maximum cache size). You can change these settings by providing a custom `CacheSettings` or `CacheManager` instance.

```java
NameMC nameMC = new NameMC(
    new ProfileRepository(new CacheSettings(Duration.of(10L, TimeUnit.MINUTES))), // Cache with time-to-live of 10 minutes and infinite maximum size.
    new ServerRepository(new CacheSettings(Duration.of(1L, TimeUnit.HOURS))) // Cache with time-to-live of 1 minute and infinite maximum size.
);
```

```java
ProfileRepository profileRepository = new ProfileRepository(new CacheSettings(Duration.of(10L, TimeUnit.MINUTES))); // Cache with time-to-live of 10 minutes and infinite maximum size.
ServerRepository serverRepository = new ServerRepository(new CacheSettings(Duration.of(1L, TimeUnit.HOURS))); // Cache with time-to-live of 1 minute and infinite maximum size.
```

```java
ProfileRepository profileRepository = new ProfileRepository(new CacheSettings(Duration.of(20L, TimeUnit.MINUTES), 200)); // Cache with time-to-live of 20 minutes and 200 maximum size.
ServerRepository serverRepository = new ServerRepository(new CacheSettings(Duration.of(2L, TimeUnit.HOURS), 20)); // Cache with time-to-live of 2 hours and 20 maximum size.
```

Now that you have access to the repositories, you can use them to get information about profiles and servers. To do that, you call the `fetch` method on the repository instance and provide a key that identifies the profile or server you want to get information about. The key is a UUID for profiles or a String representing an address for servers. In the default implementation, both profile and server repositories automatically normalize the provided key (mainly the server repository as it lowercases the address and trims leading and trailing spaces). As such, one does not need to provide a normalized key manually.

```java
Profile hypixelProfile = profileRepository.fetch(UUID.fromString("f7c77d99-9f15-4a66-a87d-c4a51ef30d19"));
```

```java
Server hypixelServer = serverRepository.fetch("mc.hypixel.net");
```

The `fetch` method returns an instance of the `Profile` or `Server` class, depending on the repository you are using. These classes contain information about the profile or server you requested. If the profile or server fetched does not exist, the method will return empty profile or server (with no friends or likes respectively).

The `fetch` method respects the cache settings you provided when creating the repository instance. The method will return the cached profile or server if it exists and is not expired. If both fail, the method will make a request to the NameMC API to get the profile or server information.

If, for some reason, you wish to bypass the cache and make a request to the NameMC API, you can use the `retrieve` method instead of the `fetch` method.

Depending on the load, fetching a profile or server may take some time. It is advised to use the `fetch` method in a separate thread or a background task to avoid blocking the main thread.

The repositories also provide direct access to the commonly used cache methods, such as `get`, `put`, `remove`, `clear` and `cleanup`, while taking care of key normalization for you. However, if you so desire, you can also directly access the repository's cache manager by calling the `getCacheManager` method. The cache is an instance of the `CacheManager` class and contains the cached profiles or servers, allowing you to manually manage the cache or access the cache settings if needed.

The `Profile` class contains the information about the UUID and friends while the `Server` class contains the information about the IP address and likes. Helper methods exist to provide a way to easily get friend data, check whether a friend is a friend of another profile or whether a server is liked by a UUID.

```java
UUID lavinoUUID = UUID.fromString("2e4a7c28-b4d4-46f9-af89-0e0fd6e1e8e6");
Server hypixelServer = serverRepository.fetch("mc.hypixel.net");
boolean hypixelServerLikedByLavino = hypixelServer.isLikedBy(lavinoUUID);
```
