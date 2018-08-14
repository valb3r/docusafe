package org.adorsys.docusafe.transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 09.07.18 at 14:06.
 */
public class SimpleRequestMemoryContextImpl implements RequestMemoryContext {
    private Map<String, TransactionalContext> pseudoUserMap = new HashMap<>();
    TransactionalContext current = null;

    @Override
    public void put(Object key, Object value) {
        current.put(key, value);
    }

    @Override
    public Object get(Object key) {
        return current.get(key);
    }

    public SimpleRequestMemoryContextImpl() {
        switchToUser(1);
    }

    public void switchToUser(int i) {
        String key = "" + i;
        if (!pseudoUserMap.containsKey(key)) {
            pseudoUserMap.put(key, new TransactionalContext());
        }
        current = pseudoUserMap.get(key);
    }

    public static class TransactionalContext extends HashMap<Object, Object> {
    }

}
