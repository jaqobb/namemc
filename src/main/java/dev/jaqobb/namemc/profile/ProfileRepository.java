package dev.jaqobb.namemc.profile;

import dev.jaqobb.namemc.cache.CacheManager;
import dev.jaqobb.namemc.cache.CacheSettings;
import dev.jaqobb.namemc.shared.BaseRepository;
import dev.jaqobb.namemc.util.HTTPUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class ProfileRepository extends BaseRepository<Profile, UUID> {
    
    private static final String FRIENDS_URL = "https://api.namemc.com/profile/%s/friends";
    
    public ProfileRepository() {
        this(new CacheSettings(Duration.of(30L, ChronoUnit.MINUTES)));
    }
    
    public ProfileRepository(CacheSettings cacheSettings) {
        super(cacheSettings);
    }
    
    public ProfileRepository(CacheManager<UUID, Profile> cacheManager) {
        super(cacheManager);
    }
    
    @Override
    public Profile retrieve(UUID key) throws Exception {
        if (key == null) {
            throw new NullPointerException("key");
        }
        JSONArray friendsJSONArray = HTTPUtils.getJSONArray(String.format(FRIENDS_URL, key));
        Collection<Friend> friends = new ArrayList<>();
        for (Object friendObject : friendsJSONArray) {
            if (!(friendObject instanceof JSONObject)) {
                continue;
            }
            JSONObject friendJSONObject = (JSONObject) friendObject;
            UUID friendUUID = UUID.fromString((String) friendJSONObject.get("uuid"));
            String friendName = (String) friendJSONObject.get("name");
            friends.add(new Friend(friendUUID, friendName));
        }
        return new Profile(key, friends);
    }
}
