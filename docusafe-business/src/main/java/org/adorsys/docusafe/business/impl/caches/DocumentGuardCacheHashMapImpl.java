package org.adorsys.docusafe.business.impl.caches;

import org.adorsys.docusafe.business.impl.DocumentGuardCache;
import org.adorsys.docusafe.service.impl.PasswordAndDocumentKeyIDWithKeyAndAccessType;

import java.util.HashMap;

/**
 * Created by peter on 14.08.18 at 14:52.
 */
public class DocumentGuardCacheHashMapImpl implements DocumentGuardCache {
    private HashMap<String, PasswordAndDocumentKeyIDWithKeyAndAccessType> map = new HashMap<>();

    @Override
    public PasswordAndDocumentKeyIDWithKeyAndAccessType get(String key) {
        return map.get(key);
    }

    @Override
    public void put(String key, PasswordAndDocumentKeyIDWithKeyAndAccessType value) {
        map.put(key, value);
    }

    @Override
    public void remove(String key) {
        map.remove(key);
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey((Object) key);
    }

    @Override
    public String toString() {
        return "" + map.keySet().size();
    }

}
