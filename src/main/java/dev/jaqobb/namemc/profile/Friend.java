package dev.jaqobb.namemc.profile;

import dev.jaqobb.namemc.server.Server;
import java.util.Objects;
import java.util.UUID;

public class Friend {
    
    private final UUID uuid;
    private final String name;
    
    public Friend(UUID uuid, String name) {
        if (uuid == null) {
            throw new NullPointerException("uuid");
        }
        if (name == null) {
            throw new NullPointerException("name");
        }
        this.uuid = uuid;
        this.name = name;
    }
    
    public UUID getUUID() {
        return this.uuid;
    }
    
    public String getName() {
        return this.name;
    }
    
    public boolean isFriendOf(Profile profile) {
        if (profile == null) {
            throw new NullPointerException("profile");
        }
        return profile.getFriend(this.uuid) != null;
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
        Friend that = (Friend) object;
        return Objects.equals(this.uuid, that.uuid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.uuid);
    }
    
    @Override
    public String toString() {
        return "Friend(uuid=" + this.uuid + ", name=" + this.name + ")";
    }
}
