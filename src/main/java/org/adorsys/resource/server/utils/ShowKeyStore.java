package org.adorsys.resource.server.utils;

import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.basetypes.ReadKeyPassword;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateCrtKey;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.KeyStore;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 02.01.18.
 */
public class ShowKeyStore {

    public static String toString(KeyStore userKeyStore, ReadKeyPassword readKeyPassword) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("Number of Entries in KeyStore is:" + userKeyStore.size());
            sb.append("\n");
            Enumeration<String> aliases = userKeyStore.aliases();
            Map<String, Integer> classToInstances = new HashMap<>();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Key key = userKeyStore.getKey(alias, readKeyPassword.getValue().toCharArray());
                sb.append("alias : " + alias + " -> " + key.toString());
                sb.append("class      " + key.getClass().getSimpleName());
                sb.append("\n");
                String c = key.getClass().getSimpleName();
                classToInstances.put(c, classToInstances.get(c) != null ? classToInstances.get(c) + 1 : 1);
                sb.append("Algorithm  " + key.getAlgorithm());
                sb.append("\n");
                sb.append("Format     " + key.getFormat());
                sb.append("\n");
                if (key instanceof SecretKeySpec) {
                    SecretKeySpec skey = (SecretKeySpec) key;
                }
                if (key instanceof BCRSAPrivateCrtKey) {
                    BCRSAPrivateCrtKey pkey = (BCRSAPrivateCrtKey) key;
                }
                sb.append("\n");
            }
            for (String key : classToInstances.keySet()) {
                sb.append(key + " -> " + classToInstances.get(key));
                sb.append("\n");
            }
            return sb.toString();

        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
