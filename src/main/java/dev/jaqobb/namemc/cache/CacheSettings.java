package dev.jaqobb.namemc.cache;

import java.time.Duration;

public class CacheSettings {
    
    private final Duration duration;
    
    public CacheSettings(Duration duration) {
        if (duration == null) {
            throw new NullPointerException("duration");
        }
        this.duration = duration;
    }
    
    public Duration getDuration() {
        return this.duration;
    }
}
