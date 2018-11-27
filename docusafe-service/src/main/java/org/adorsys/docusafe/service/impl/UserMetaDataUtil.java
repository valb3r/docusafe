package org.adorsys.docusafe.service.impl;

import org.adorsys.encobject.domain.UserMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 27.11.18 09:36.
 */
public class UserMetaDataUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserMetaDataUtil.class);

    public static void setNoEncryption(UserMetaData userMetaData) {
        userMetaData.put("NO_ENCRYPTION", "TRUE");
    }

    public static boolean isNotEncrypted(UserMetaData userMetaData) {
        String value = null;
        if ((value = userMetaData.find("NO_ENCRYPTION")) != null) {
            return (value.equalsIgnoreCase("TRUE"));
        }

        if (System.getProperty("UGLY_UGLY_NO_ENCRYPTION_AT_ALL_NEVER_TO_BE_USED", "false").equalsIgnoreCase("true")) {
            LOGGER.warn("ENCRYPTION DISABLED BY SYSTEM PROPERTY");
            UserMetaDataUtil.setNoEncryption(userMetaData);
            return true;
        }

        return false;
    }


}
