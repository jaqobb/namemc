package dev.jaqobb.namemc.server;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class Server {
    
    private final String address;
    private final Collection<UUID> likes;
    
    public Server(String address, Collection<UUID> likes) {
        if (address == null) {
            throw new NullPointerException("address");
        }
        if (likes == null) {
            throw new NullPointerException("likes");
        }
        this.address = address;
        this.likes = likes;
    }
    
    public String getAddress() {
        return this.address;
    }
    
    public Collection<UUID> getLikes() {
        return Collections.unmodifiableCollection(this.likes);
    }
    
    public boolean isLikedBy(UUID uuid) {
        return this.likes.contains(uuid);
    }
    
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        Server that = (Server) object;
        return Objects.equals(this.address, that.address);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.address);
    }
    
    @Override
    public String toString() {
        return "Server(address=" + this.address + ", likes=" + this.likes + ")";
    }
}
