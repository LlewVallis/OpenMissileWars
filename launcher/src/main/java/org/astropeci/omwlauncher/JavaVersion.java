package org.astropeci.omwlauncher;

import lombok.experimental.UtilityClass;

/**
 * Utility class for working with Java versions.
 */
@UtilityClass
public class JavaVersion {

    public boolean isJavaOutdated() {
        // Fetch the Java version string. This is usually of the form "major.minor.patch". We'll attempt to read the
        // major version to determine whether Java is up to date
        String versionString = System.getProperty("java.version");
        // Where the major version ends, or -1 if it takes up the entire string
        int firstPeriod = versionString.indexOf(".");

        // Trim the string to just the major version number
        if (firstPeriod > -1) {
            versionString = versionString.substring(0, firstPeriod);
        }

        try {
            // Attempt to interpret the major version number as an integer. This will be 1 for Java 1.8, 9 for Java 9,
            // 11 for Java 11 etc
            int leadingNumber = Integer.parseInt(versionString);
            return leadingNumber < 11;
        } catch (NumberFormatException e) {
            // If we can't parse the Java version, assume its fine
            return false;
        }
    }
}
