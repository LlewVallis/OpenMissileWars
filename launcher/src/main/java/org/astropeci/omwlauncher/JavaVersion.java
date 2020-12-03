package org.astropeci.omwlauncher;

import lombok.experimental.UtilityClass;

@UtilityClass
public class JavaVersion {

    public boolean isJavaOutdated() {
        String versionString = System.getProperty("java.version");
        int firstPeriod = versionString.indexOf(".");

        if (firstPeriod > -1) {
            versionString = versionString.substring(0, firstPeriod);
        }

        try {
            int leadingNumber = Integer.parseInt(versionString);
            return leadingNumber < 11;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
