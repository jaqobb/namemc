package dev.jaqobb.namemc.profile;

import dev.jaqobb.namemc.cache.CacheSettings;
import org.junit.jupiter.api.BeforeEach;
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
    
    private ProfileRepository profileRepository;
    
    @BeforeEach
    void setup() {
        this.profileRepository = new ProfileRepository(new CacheSettings(Duration.of(2L, ChronoUnit.SECONDS)));
    }
    
    @Test
    void testSuccessfulFetch() {
        UUID hypixelUUID = UUID.fromString("f7c77d99-9f15-4a66-a87d-c4a51ef30d19");
        Profile hypixelProfile = this.profileRepository.fetch(hypixelUUID);
        assertNotNull(hypixelProfile);
        assertEquals(hypixelUUID, hypixelProfile.getUUID());
        assertEquals(0, hypixelProfile.getFriends().size());
        UUID lovinoUUID = UUID.fromString("2e4a7c28-b4d4-46f9-af89-0e0fd6e1e8e6");
        Profile lovinoProfile = this.profileRepository.fetch(lovinoUUID);
        assertNotNull(lovinoProfile);
        assertEquals(lovinoUUID, lovinoProfile.getUUID());
        assertNotEquals(0, lovinoProfile.getFriends().size());
    }
    
    @Test
    void testCache() {
        UUID hypixelUUID = UUID.fromString("f7c77d99-9f15-4a66-a87d-c4a51ef30d19");
        Profile hypixelProfile = this.profileRepository.fetch(hypixelUUID);
        assertEquals(hypixelProfile, this.profileRepository.getCacheManager().get(hypixelUUID));
        await().atMost(3L, TimeUnit.SECONDS).until(() -> this.profileRepository.getCacheManager().get(hypixelUUID) == null);
        assertNull(this.profileRepository.getCacheManager().get(hypixelUUID));
    }
    
    @Test
    void testCacheCleanup() {
        UUID hypixelUUID = UUID.fromString("f7c77d99-9f15-4a66-a87d-c4a51ef30d19");
        UUID lovinoUUID = UUID.fromString("2e4a7c28-b4d4-46f9-af89-0e0fd6e1e8e6");
        this.profileRepository.fetch(hypixelUUID);
        this.profileRepository.fetch(lovinoUUID);
        assertEquals(2, this.profileRepository.getCacheManager().getCache().size());
        await().atMost(3L, TimeUnit.SECONDS).until(() -> {
            this.profileRepository.getCacheManager().cleanup();
            return this.profileRepository.getCacheManager().getCache().isEmpty();
        });
        assertEquals(0, this.profileRepository.getCacheManager().getCache().size());
    }
}
