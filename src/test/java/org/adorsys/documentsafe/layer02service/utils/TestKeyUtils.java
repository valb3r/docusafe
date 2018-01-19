package org.adorsys.documentsafe.layer02service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class TestKeyUtils {
	private final static Logger LOGGER = LoggerFactory.getLogger(TestKeyUtils.class);
	
	public static void turnOffEncPolicy(){
		// Warning: do not do this for productive code. Download and install the jce unlimited strength policy file
		// see http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
		try {
	        Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
	        field.setAccessible(true);
	        field.set(null, Boolean.FALSE);
	    } catch (Exception e) {
	        // It is fine, to ignore and suppress any Exception here
	    }
	    finally {
			LOGGER.warn("WARNING: Encoding Policy has been switched off for test");
		}
	}
}
