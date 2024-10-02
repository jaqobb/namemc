package dev.jaqobb.namemc.cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class CacheManager<K, V> {
    
    private final CacheSettings cacheSettings;
    private final Map<K, CacheEntry<V>> cache;
    
    public CacheManager(CacheSettings cacheSettings) {
        if (cacheSettings == null) {
            throw new NullPointerException("cacheSettings");
        }
        this.cacheSettings = cacheSettings;
        int initialCapacity = this.cacheSettings.getMaxSize() > 0 ? this.cacheSettings.getMaxSize() : 16;
        float loadFactor = this.cacheSettings.getMaxSize() > 0 ? 1.0F : 0.75F;
        this.cache = Collections.synchronizedMap(new LinkedHashMap<K, CacheEntry<V>>(initialCapacity, loadFactor, true) {
            
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, CacheEntry<V>> eldest) {
                return CacheManager.this.cacheSettings.getMaxSize() > 0 && this.size() > CacheManager.this.cacheSettings.getMaxSize();
            }
        });
    }
    
    public CacheSettings getCacheSettings() {
        return this.cacheSettings;
    }
    
    public Map<K, CacheEntry<V>> getCache() {
        return Collections.unmodifiableMap(this.cache);
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
