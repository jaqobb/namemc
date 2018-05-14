##  NameMC-API
Java wrapper for the popular Minecraft related site: https://namemc.com

### Purpose of this project
Basically, this API doesn't support much (but everything it actually can due to lack of API from NameMC creators).

What are the purposes of this API? Well, you can reward players on your Minecraft server that have liked your server on the NameMC. Besides that, you can easily handle friends of your players and add some interactaction between them, like every 5 new friends on the NameMC, player will receive 5 diamonds and so on.

### Requirements
All you need to properly use this API is Java 8 and Maven.

### How to use
First of all, you need to add repository to your project's pom.xml:
```xml
<repositories>
	<repository>
		<id>jaqobb-repo</id>
		<url>https://repo.jaqobb.co/repository/maven-snapshots/</url>
	</repository>
</repositories>
```
And then add dependency:
```xml
<dependencies>
	<dependency>
		<groupId>co.jaqobb</groupId>
		<artifactId>namemc-api</artifactId>
		<version>1.0-SNAPSHOT</version>
		<scope>compile</scope>
	</dependency>
</dependencies>
```
Yeah, you actually need to compile this library due to it's not shaded anywhere by default.
### API
Everythings begins with the class NameMC. You can either use preset settings created by me, or experiment with settings numbers on your own:
```java
NameMC.newDefault();
```
to create a new instance of NameMC class with default settings, or:
```java
NameMC.newCustom(PprofileService profileService, SserverService serverService);
```
to create a new instance of NameMC class with custom settings.

But what the hell is profile and server service? They are being used to store cached profiles and servers and give you possibility to lookup a new ones.

At this moment you are able to only change time between which cached profiles and servers don't need to be recached again.

You use both the profile and the server service in the same way.

You create profile service with either:
```java
ProfileService.newDefault();
```
to create profile service with default values being 5 as a time and minutes as a unit, or:
```java
ProfileService.newCustom(long time, TimeUnit unit);
```
to create profile service with custom time and unit.

The same thing goes for the server service, just replace "Profile" with "Server".

When you created NameMC class instance, you can now use profile and server service to lookup either a profile or a server:
```java
nameMC.profileService().lookup(UUID uniqueId, boolean recache, BiConsumer<Profile, Exception> callback);
```
Unique id is the unique id of the profile you want to lookup.
Recache defines if the profile should be forced to be recached even if there is already a cached profile with the given unique id and if the cache time was smaller than store time.
Callback returns you a profile filled with all information that could possibly be get about the profile (unique id of this profile, all friends and the time this profile was cached at) and exception which is null if the error didn't occur when trying to lookup the profile.

When you have your profile ready, you can access some nice methods:
```java
// Returns unique id of the profile.
getUniqueId();

// Returns an immutable collection of friends.
// In a Friend class, you can use getUniqueId to get friend's unique id or getName() to get friend's name.
getFriends();

// Returns friend with the given unique id, or null if the profile doesn't have a friend with the given unique id.
getFriend(UUID uniqueId);

// Returns friend with the given name (case sensitive), or null if the profile doesn't have a friend with the given name.
getFriend(String name);

// Returns friend with the given name (case sensitive or not), or null if the profile doesn't have a friend with the given name.
getFriend(String name, boolean caseSensitive);

// Returns true if the profile has a friend with the given unique id, false otherwise.
hasFriend(UUID uniqueId);

// Returns true if the profile has a friend with the given name (case sensitive), false otherwise.
hasFriend(String name);

// Returns true if the profile has a friend with the given name (case sensitive or not), false otherwise.
hasFriend(String name, boolean caseSensitive);

// Returns time the profile was cached at.
getCacheTime();
```
or:
```java
nameMC.profileService().lookup(String ip, boolean recache, BiConsumer<Server, Exception> callback);
```
Ip is the ip of the server you want to lookup (case insensitive).
Recache defines if the server should be forced to be recached even if there is already a cached server with the given ip and if the cache time was smaller than store time.
Callback returns you a server filled with all information that could possibly be get about the server (ip of this server, all unique ids that have liked this server and the time this server was cached at) and exception which is null if the error didn't occur when trying to lookup the server.

When you have your server ready, you can access some nice methods:
```java
// Returns ip of the server.
getIp();

// Returns an immutable collection of unique ids that have liked the server.
getLikes();

// Returns true if the given unique id has liked the server, false otherwise.
hasLiked(UUID uniqueId);

// Returns time the server was cached at.
getCacheTime();
```

You can also get all cached profiles or servers by using either:
```java
nameMC.playerService().all();
```
or:
```java
nameMC.serverService().all();
```

### End
That's all! Thank you for using NameMC-API!