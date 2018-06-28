package org.adorsys.docusafe.business.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.types.MemoryContext;
import org.adorsys.docusafe.service.impl.DocumentGuardServiceImpl;
import org.bouncycastle.jcajce.provider.symmetric.ARC4;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;

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

    private static String pump(int l, String s, String filler) {
        int rem = l-s.length();
        if (rem < 0) {
            throw new BaseException("programming error: " + l + " is < length of " + s);
        }
        String ret = s;
        while (rem-- > 0) {
            s = s + filler;
        }
        return s;
    }

    public static String toString(MemoryContext mc) {
        if (mc == null) {
            return "MemoryContext is NULL";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("CACHE");
        sb.append("\n");
        sb.append(showMap(mc.get(DocumentGuardServiceImpl.GUARD_MAP)));
        sb.append(showMap(mc.get(DocumentSafeServiceImpl.USER_AUTH_CACHE)));
        return sb.toString();
    }

    private static String showMap(Object o) {
        if (o == null) {
            return "{}";
        }
        if (!(o instanceof Map)) {
            return "can not show content of " + o.getClass().getCanonicalName();
        }
        Map map = (Map) o;
        StringBuilder sb = new StringBuilder();
        sb.append("\n");

        int l = 0;
        for (Object key : map.keySet()) {
            if (l<key.toString().length()) {
                l = key.toString().length();
            }
            if (l<key.getClass().getName().length()) {
                l = key.getClass().getName().length();
            }
        }

        boolean first = true;
        for (Object key : map.keySet()) {
            Object stringKey = key;
            Object value = map.get(key);
            if (first) {
                sb.append(pump(l, key.getClass().getName()) + " : " + value.getClass().getName());
                sb.append("\n");
                sb.append(pump(l, "", "-"));
                sb.append("\n");
                first = false;
            }
            sb.append(pump(l, key.toString()) + " : " + value.toString());
            sb.append("\n");
        };
        return sb.toString();
    }
    private static String pump(int l, String s) {
        return pump(l,s, " ");
    }
}
