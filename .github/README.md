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
        <version>3.0.0</version>
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
    implementation("dev.jaqobb:namemc:3.0.0")   
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
    implementation "dev.jaqobb:namemc:3.0.0"
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

You can use different available constructors to create instances of the repositories with modified cache settings. By default, the repositories use a cache with a time-to-live of 30 minutes. You can change these settings by providing a custom `CacheSettings` or `CacheManager` instance.

```java
NameMC nameMC = new NameMC(
    new ProfileRepository(new CacheSettings(10L, TimeUnit.MINUTES)),
    new ServerRepository(new CacheSettings(1L, TimeUnit.HOURS))
);
```

```java
ProfileRepository profileRepository = new ProfileRepository(new CacheSettings(10L, TimeUnit.MINUTES));
ServerRepository serverRepository = new ServerRepository(new CacheSettings(1L, TimeUnit.HOURS));
```

Now that you have access to the repositories, you can use them to get information about profiles and servers. To do that, you call the `fetch` method on the repository instance and provide a key that identifies the profile or server you want to get information about. The key is a UUID for profiles or a String representing an IP address for servers. It is advised to provide a normalized key (i.e. IP address lowercased) to avoid any issues with caching.

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

You can also directly access the current cache of the repository by calling the `getCacheManager` method. The cache is an instance of the `CacheManager` class and contains the cached profiles or servers, allowing you to manually manage the cache if needed or access the cache settings.

The `Profile` class contains the information about the UUID and friends while the `Server` class contains the information about the IP address and likes. Helper methods exist to provide a way to easily get friend data, check whether a friend is a friend of another profile or whether a server is liked by a UUID.

```java
UUID lavinoUUID = UUID.fromString("2e4a7c28-b4d4-46f9-af89-0e0fd6e1e8e6");
Server hypixelServer = serverRepository.fetch("mc.hypixel.net");
boolean hypixelServerLikedByLavino = hypixelServer.isLikedBy(lavinoUUID);
```
