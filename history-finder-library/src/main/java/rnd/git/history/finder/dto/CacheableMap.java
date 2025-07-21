package rnd.git.history.finder.dto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @since 11/6/24
 **/
public class CacheableMap<K, V> extends LinkedHashMap<K, V> {
    private final int  maxSize;
    public CacheableMap(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}
