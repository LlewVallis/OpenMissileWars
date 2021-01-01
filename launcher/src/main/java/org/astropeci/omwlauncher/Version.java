package org.astropeci.omwlauncher;

import lombok.Value;

import java.util.Optional;

/**
 * A version of OpenMissileWars which may or may not exist.
 */
@Value
public class Version {

    /**
     * The version number, starting at 1.
     */
    int index;

    /**
     * Attempts to parse a user input string into a version.
     *
     * If the version is simply malformed, an empty option will be returned. However, if the version is syntactically
     * valid but illegal (e.g. version -1) a {@link ResponseException} will be thrown.
     */
    public static Optional<Version> parse(String input) {
        // "prebuilt-" is used in the GitHub release names, so it makes sense ignore that prefix if given
        if (input.startsWith("prebuilt-")) {
            input = input.substring("prebuilt-".length());
        }

        try {
            int index = Integer.parseInt(input);
            if (index < 1) {
                throw new ResponseException("Version numbers start at 1");
            }

            return Optional.of(new Version(index));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
