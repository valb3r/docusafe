package org.adorsys.docusafe.business.types;

/**
 * Created by peter on 26.06.18 at 17:55.
 */
public interface MemoryContext {
    void put(Object key, Object value);
    Object get(Object key);
}
