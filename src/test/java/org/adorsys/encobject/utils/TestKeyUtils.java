package org.adorsys.encobject.utils;

import java.lang.reflect.Field;

public class TestKeyUtils {
	
	public static void turnOffEncPolicy(){
		// Warning: do not do this for productive code. Download and install the jce unlimited strength policy file
		// see http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
		try {
	        Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
	        field.setAccessible(true);
	        field.set(null, Boolean.FALSE);
	    } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
	        // ex.printStackTrace(System.err);
	    }
	    finally {
			System.err.println("WARNING: Encoding Policy has been switched off for test");
		}
	}
}
