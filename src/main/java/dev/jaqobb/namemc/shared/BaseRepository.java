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
    
    public abstract K normalizeKey(K key);
    
    public T get(K key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        return this.cacheManager.get(this.normalizeKey(key));
    }
    
    public void put(K key, T value) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (value == null) {
            throw new NullPointerException("value");
        }
        this.cacheManager.put(this.normalizeKey(key), value);
    }
    
    public void remove(K key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        this.cacheManager.getCache().remove(this.normalizeKey(key));
    }
    
    public void clear() {
        this.cacheManager.getCache().clear();
    }
    
    public void cleanup() {
        this.cacheManager.getCache().entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    public T fetch(K key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        key = this.normalizeKey(key);
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
