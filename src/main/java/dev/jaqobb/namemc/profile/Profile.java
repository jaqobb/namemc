package dev.jaqobb.namemc.profile;

import dev.jaqobb.namemc.server.Server;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Profile {
    
    private final UUID uuid;
    private final Map<UUID, Friend> friendsByUUID;
    private final Map<String, Friend> friendsByName;
    
    public Profile(UUID uuid, Collection<Friend> friends) {
        if (uuid == null) {
            throw new NullPointerException("uuid");
        }
        if (friends == null) {
            throw new NullPointerException("friends");
        }
        this.uuid = uuid;
        this.friendsByUUID = new HashMap<>();
        this.friendsByName = new HashMap<>();
        for (Friend friend : friends) {
            this.friendsByUUID.put(friend.getUUID(), friend);
            this.friendsByName.put(friend.getName().toLowerCase(), friend);
        }
    }
    
    public UUID getUUID() {
        return this.uuid;
    }
    
    public Collection<Friend> getFriends() {
        return new ArrayList<>(this.friendsByUUID.values());
    }
    
    public Friend getFriend(UUID uuid) {
        if (uuid == null) {
            throw new NullPointerException("uuid");
        }
        return this.friendsByUUID.get(uuid);
    }
    
    public Friend getFriend(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        return this.friendsByName.get(name.toLowerCase());
    }
    
    public boolean hasLikedServer(Server server) {
        if (server == null) {
            throw new NullPointerException("server");
        }
        return server.isLikedBy(this.uuid);
    }
    
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        Profile that = (Profile) object;
        return Objects.equals(this.uuid, that.uuid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.uuid);
    }
    
    @Override
    public String toString() {
        return "Profile(uuid=" + this.uuid + ", friends=" + this.friendsByUUID.values() + ")";
    }
}
