package org.astropeci.omwlauncher;

import lombok.Value;

import java.util.Optional;

@Value
public class Version {

    int index;

    public static Optional<Version> parse(String input) {
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
