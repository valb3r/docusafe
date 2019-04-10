package org.adorsys.docusafe.business.impl.caches.hashmap;

import org.adorsys.docusafe.business.impl.caches.DocusafeCacheTemplate;
import java.util.HashMap;

/**
 * Created by peter on 14.08.18 at 16:24.
 */
public class DocusafeCacheTemplateHashMapImpl<K,V> implements DocusafeCacheTemplate<K,V> {
    private HashMap<K,V> map = new HashMap<>();

    @Override
    public V get(K key) {
        return map.get(key);
    }

    @Override
    public void put(K key, V value) {
        map.put(key, value);
    }

    @Override
    public void remove(K key) {
        map.remove(key);
    }

    @Override
    public long size() {
        return map.size();
    }

    @Override
    public void clear() {
        map.clear();
    }
}
