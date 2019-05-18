package scraper.utils;


import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentCacheHashMap<K,V> extends ConcurrentHashMap<K, V> {
    private int limit = 1000;

    @Override
    public V put(K key, V value) {
        if(size() > limit) clear();

        return super.put(key, value);
    }
}
