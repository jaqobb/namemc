package dev.jaqobb.namemc.server;

import dev.jaqobb.namemc.cache.CacheSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerRepositoryTest {
    
    private ServerRepository serverRepository;
    
    @BeforeEach
    void setup() {
        this.serverRepository = new ServerRepository(new CacheSettings(Duration.of(2L, ChronoUnit.SECONDS)));
    }
    
    @Test
    void testSuccessfulFetch() {
        Server hypixelServer = this.serverRepository.fetch("mc.hypixel.net");
        assertNotNull(hypixelServer);
        assertEquals("mc.hypixel.net", hypixelServer.getAddress());
        assertNotEquals(0, hypixelServer.getLikes().size());
        assertTrue(hypixelServer.isLikedBy(UUID.fromString("2e4a7c28-b4d4-46f9-af89-0e0fd6e1e8e6")));
        Server invalidServer = this.serverRepository.fetch("invalid");
        assertNotNull(invalidServer);
        assertEquals("invalid", invalidServer.getAddress());
        assertEquals(0, invalidServer.getLikes().size());
        assertFalse(invalidServer.isLikedBy(UUID.fromString("2e4a7c28-b4d4-46f9-af89-0e0fd6e1e8e6")));
    }
    
    @Test
    void testCache() {
        Server hypixelServer = this.serverRepository.fetch("mc.hypixel.net");
        assertEquals(hypixelServer, this.serverRepository.getCacheManager().get("mc.hypixel.net"));
        await().atMost(3L, TimeUnit.SECONDS).until(() -> this.serverRepository.getCacheManager().get("mc.hypixel.net") == null);
        assertNull(this.serverRepository.getCacheManager().get("mc.hypixel.net"));
    }
    
    @Test
    void testCacheCleanup() {
        this.serverRepository.fetch("mc.hypixel.net");
        this.serverRepository.fetch("invalid");
        assertEquals(2, this.serverRepository.getCacheManager().getCache().size());
        await().atMost(3L, TimeUnit.SECONDS).until(() -> {
            this.serverRepository.getCacheManager().cleanup();
            return this.serverRepository.getCacheManager().getCache().isEmpty();
        });
        assertEquals(0, this.serverRepository.getCacheManager().getCache().size());
    }
}
