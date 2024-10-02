package dev.jaqobb.namemc.profile;

import dev.jaqobb.namemc.cache.CacheSettings;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProfileRepositoryTest {
    
    @Test
    void testFetch() {
        ProfileRepository profileRepository = new ProfileRepository(new CacheSettings(Duration.of(2L, ChronoUnit.SECONDS)));
        UUID hypixelUUID = UUID.fromString("f7c77d99-9f15-4a66-a87d-c4a51ef30d19");
        Profile hypixelProfile = profileRepository.fetch(hypixelUUID);
        assertEquals(hypixelUUID, hypixelProfile.getUUID());
        assertEquals(0, hypixelProfile.getFriends().size());
        UUID lovinoUUID = UUID.fromString("2e4a7c28-b4d4-46f9-af89-0e0fd6e1e8e6");
        Profile lovinoProfile = profileRepository.fetch(lovinoUUID);
        assertEquals(lovinoUUID, lovinoProfile.getUUID());
        assertNotEquals(0, lovinoProfile.getFriends().size());
    }
    
    @Test
    void testCache() {
        ProfileRepository profileRepository = new ProfileRepository(new CacheSettings(Duration.of(2L, ChronoUnit.SECONDS)));
        UUID hypixelUUID = UUID.fromString("f7c77d99-9f15-4a66-a87d-c4a51ef30d19");
        Profile hypixelProfile = profileRepository.fetch(hypixelUUID);
        assertEquals(hypixelProfile, profileRepository.getCacheManager().get(hypixelUUID));
        await().atMost(3L, TimeUnit.SECONDS).until(() -> profileRepository.get(hypixelUUID) == null);
        assertNull(profileRepository.getCacheManager().get(hypixelUUID));
    }
    
    @Test
    void testCacheCleanup() {
        ProfileRepository profileRepository = new ProfileRepository(new CacheSettings(Duration.of(2L, ChronoUnit.SECONDS)));
        UUID hypixelUUID = UUID.fromString("f7c77d99-9f15-4a66-a87d-c4a51ef30d19");
        UUID lovinoUUID = UUID.fromString("2e4a7c28-b4d4-46f9-af89-0e0fd6e1e8e6");
        profileRepository.fetch(hypixelUUID);
        profileRepository.fetch(lovinoUUID);
        assertEquals(2, profileRepository.getCacheManager().getCache().size());
        await().atMost(3L, TimeUnit.SECONDS).until(() -> {
            profileRepository.getCacheManager().cleanup();
            return profileRepository.getCacheManager().getCache().isEmpty();
        });
        assertEquals(0, profileRepository.getCacheManager().getCache().size());
    }
    
    @Test
    void testCacheMaxSize() {
        ProfileRepository profileRepository = new ProfileRepository(new CacheSettings(Duration.of(10L, ChronoUnit.MINUTES), 1));
        UUID hypixelUUID = UUID.fromString("f7c77d99-9f15-4a66-a87d-c4a51ef30d19");
        UUID lovinoUUID = UUID.fromString("2e4a7c28-b4d4-46f9-af89-0e0fd6e1e8e6");
        profileRepository.fetch(hypixelUUID);
        assertEquals(1, profileRepository.getCacheManager().getCache().size());
        profileRepository.fetch(lovinoUUID);
        assertEquals(1, profileRepository.getCacheManager().getCache().size());
        assertNull(profileRepository.getCacheManager().get(hypixelUUID));
        assertNotNull(profileRepository.getCacheManager().get(lovinoUUID));
    }
}
