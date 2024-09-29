package dev.jaqobb.namemc.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class CacheEntry<V> {
    
    private final V value;
    private final long expirationTimestamp;
    
    public CacheEntry(V value, Duration duration) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        if (duration == null) {
            throw new NullPointerException("duration");
        }
        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        this.value = value;
        this.expirationTimestamp = Instant.now().plus(duration).toEpochMilli();
    }
    
    public V getValue() {
        return this.value;
    }
    
    public boolean isExpired() {
        return Instant.now().toEpochMilli() >= this.expirationTimestamp;
    }
    
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        CacheEntry<?> that = (CacheEntry<?>) object;
        return this.expirationTimestamp == that.expirationTimestamp && Objects.equals(this.value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.value, this.expirationTimestamp);
    }
    
    @Override
    public String toString() {
        return "CacheEntry(value=" + this.value + ", expirationTimestamp=" + this.expirationTimestamp + ")";
    }
}
