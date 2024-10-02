package dev.jaqobb.namemc.server;

import dev.jaqobb.namemc.cache.CacheSettings;
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
    
    @Test
    void testFetch() {
        ServerRepository serverRepository = new ServerRepository(new CacheSettings(Duration.of(2L, ChronoUnit.SECONDS)));
        Server hypixelServer = serverRepository.fetch("mc.hypixel.net");
        assertEquals("mc.hypixel.net", hypixelServer.getAddress());
        assertNotEquals(0, hypixelServer.getLikes().size());
        assertTrue(hypixelServer.isLikedBy(UUID.fromString("2e4a7c28-b4d4-46f9-af89-0e0fd6e1e8e6")));
        Server invalidServer = serverRepository.fetch("invalid");
        assertEquals("invalid", invalidServer.getAddress());
        assertEquals(0, invalidServer.getLikes().size());
        assertFalse(invalidServer.isLikedBy(UUID.fromString("2e4a7c28-b4d4-46f9-af89-0e0fd6e1e8e6")));
    }
    
    @Test
    void testCache() {
        ServerRepository serverRepository = new ServerRepository(new CacheSettings(Duration.of(2L, ChronoUnit.SECONDS)));
        Server hypixelServer = serverRepository.fetch("mc.hypixel.net");
        assertEquals(hypixelServer, serverRepository.get("mc.hypixel.net"));
        assertEquals(hypixelServer, serverRepository.get("MC.HYPIXEL.NET"));
        await().atMost(3L, TimeUnit.SECONDS).until(() -> serverRepository.get("mc.hypixel.net") == null && serverRepository.get("MC.HYPIXEL.NET") == null);
        assertNull(serverRepository.get("mc.hypixel.net"));
        assertNull(serverRepository.get("MC.HYPIXEL.NET"));
    }
    
    @Test
    void testCacheCleanup() {
        ServerRepository serverRepository = new ServerRepository(new CacheSettings(Duration.of(2L, ChronoUnit.SECONDS)));
        serverRepository.fetch("mc.hypixel.net");
        serverRepository.fetch("invalid");
        assertEquals(2, serverRepository.getCacheManager().getCache().size());
        await().atMost(3L, TimeUnit.SECONDS).until(() -> {
            serverRepository.getCacheManager().cleanup();
            return serverRepository.getCacheManager().getCache().isEmpty();
        });
        assertEquals(0, serverRepository.getCacheManager().getCache().size());
    }
    
    @Test
    void testCacheMaxSize() {
        ServerRepository serverRepository = new ServerRepository(new CacheSettings(Duration.of(10L, ChronoUnit.MINUTES), 1));
        serverRepository.fetch("mc.hypixel.net");
        assertEquals(1, serverRepository.getCacheManager().getCache().size());
        serverRepository.fetch("invalid");
        assertEquals(1, serverRepository.getCacheManager().getCache().size());
        assertNull(serverRepository.get("mc.hypixel.net"));
        assertNotNull(serverRepository.get("invalid"));
    }
}
