package dev.jaqobb.namemc.cache;

import java.time.Duration;

public class CacheSettings {
    
    private final Duration duration;
    private final int maxSize;
    
    public CacheSettings(Duration duration) {
        this(duration, -1);
    }
    
    public CacheSettings(Duration duration, int maxSize) {
        if (duration == null) {
            throw new NullPointerException("duration");
        }
        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        this.duration = duration;
        this.maxSize = maxSize;
    }
    
    public Duration getDuration() {
        return this.duration;
    }
    
    public int getMaxSize() {
        return this.maxSize;
    }
}
