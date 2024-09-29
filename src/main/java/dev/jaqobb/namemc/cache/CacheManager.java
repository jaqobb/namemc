package dev.jaqobb.namemc.cache;

import java.util.concurrent.ConcurrentHashMap;

public class CacheManager<K, V> {
    
    private final CacheSettings cacheSettings;
    private final ConcurrentHashMap<K, CacheEntry<V>> cache;
    
    public CacheManager(CacheSettings cacheSettings) {
        if (cacheSettings == null) {
            throw new NullPointerException("cacheSettings");
        }
        this.cacheSettings = cacheSettings;
        this.cache = new ConcurrentHashMap<>();
    }
    
    public CacheSettings getCacheSettings() {
        return this.cacheSettings;
    }
    
    public ConcurrentHashMap<K, CacheEntry<V>> getCache() {
        return new ConcurrentHashMap<>(this.cache);
    }
    
    public V get(K key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        CacheEntry<V> entry = this.cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            this.cache.remove(key);
            return null;
        }
        return entry.getValue();
    }
    
    public void put(K key, V value) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (value == null) {
            throw new NullPointerException("value");
        }
        this.cache.put(key, new CacheEntry<>(value, this.cacheSettings.getDuration()));
    }
    
    public void remove(K key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        this.cache.remove(key);
    }
    
    public void clear() {
        this.cache.clear();
    }
    
    public void cleanup() {
        this.cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
