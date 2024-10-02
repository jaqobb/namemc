package dev.jaqobb.namemc.server;

import dev.jaqobb.namemc.cache.CacheManager;
import dev.jaqobb.namemc.cache.CacheSettings;
import dev.jaqobb.namemc.shared.BaseRepository;
import dev.jaqobb.namemc.util.HTTPUtils;
import org.json.simple.JSONArray;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class ServerRepository extends BaseRepository<Server, String> {
    
    private static final String LIKES_URL = "https://api.namemc.com/server/%s/likes";
    
    public ServerRepository() {
        this(new CacheSettings(Duration.of(30L, ChronoUnit.MINUTES)));
    }
    
    public ServerRepository(CacheSettings cacheSettings) {
        super(cacheSettings);
    }
    
    public ServerRepository(CacheManager<String, Server> cacheManager) {
        super(cacheManager);
    }
    
    @Override
    public String normalizeKey(String key) {
        return key.toLowerCase().trim();
    }
    
    @Override
    public Server retrieve(String key) throws Exception {
        if (key == null) {
            throw new NullPointerException("key");
        }
        key = this.normalizeKey(key);
        JSONArray array = HTTPUtils.getJSONArray(String.format(LIKES_URL, key));
        Collection<UUID> likes = new ArrayList<>();
        for (Object likeObject : array) {
            if (!(likeObject instanceof String)) {
                continue;
            }
            likes.add(UUID.fromString((String) likeObject));
        }
        return new Server(key, likes);
    }
}
