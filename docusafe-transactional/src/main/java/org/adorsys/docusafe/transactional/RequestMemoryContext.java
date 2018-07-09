package org.adorsys.docusafe.transactional;

/**
 * Created by peter on 09.07.18 at 11:37.
 */
public interface RequestMemoryContext {
    void put(Object key, Object value);
    Object get(Object key);
}
