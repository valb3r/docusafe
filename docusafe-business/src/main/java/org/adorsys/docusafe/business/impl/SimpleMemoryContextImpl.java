package org.adorsys.docusafe.business.impl;

import org.adorsys.docusafe.business.types.MemoryContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 26.06.18 at 17:56.
 */
public class SimpleMemoryContextImpl implements MemoryContext {
    Map<Object, Object> map = new HashMap<>();
    @Override
    public void put(Object key, Object value) {
        map.put(key, value);
    }

    @Override
    public Object get(Object key) {
        return map.get(key);
    }
}
