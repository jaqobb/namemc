package dev.jaqobb.namemc.shared;

import dev.jaqobb.namemc.cache.CacheManager;
import dev.jaqobb.namemc.cache.CacheSettings;

public abstract class BaseRepository<T, K> {
    
    private final CacheManager<K, T> cacheManager;
    
    public BaseRepository(CacheSettings cacheSettings) {
        if (cacheSettings == null) {
            throw new NullPointerException("cacheSetting");
        }
        this.cacheManager = new CacheManager<>(cacheSettings);
    }
    
    public BaseRepository(CacheManager<K, T> cacheManager) {
        if (cacheManager == null) {
            throw new NullPointerException("cacheManager");
        }
        this.cacheManager = cacheManager;
    }
    
    public CacheManager<K, T> getCacheManager() {
        return this.cacheManager;
    }
    
    public T fetch(K key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        T cachedValue = this.cacheManager.get(key);
        if (cachedValue != null) {
            return cachedValue;
        }
        T fetchedValue;
        try {
            fetchedValue = this.retrieve(key);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to fetch value for key: " + key, exception);
        }
        if (fetchedValue != null) {
            this.cacheManager.put(key, fetchedValue);
        }
        return fetchedValue;
    }
    
    public abstract T retrieve(K key) throws Exception;
}
