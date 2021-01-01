package org.astropeci.omwlauncher;

import lombok.AllArgsConstructor;

import java.io.IOException;

/**
 * Manages the lifecycle of the launcher when it is used in CLI mode.
 *
 * Unlike the GUI launcher, the CLI launcher remains active while the server process is running.
 */
@AllArgsConstructor
public class CliFlow {

    /**
     * The string passed through the version CLI flag, or an empty string if it was not specified.
     */
    private final String versionInput;

    /**
     * True if the EULA has been accepted via the CLI.
     */
    private final boolean acceptEula;

    /**
     * The amount of memory explicitly set via the CLI, or the default amount if it was not set.
     */
    private final int memory;

    private final GitHubAccess gitHubAccess = new GitHubAccess();
    private final VersionManager versionManager = new VersionManager();

    public void run() {
        // Determine the actual version that needs to be used. If the version was specified via the command line, it
        // will be parsed. Otherwise, the user will be prompted for the version they would like to use
        Version version;
        if (versionInput.isEmpty()) {
            version = queryVersion();
        } else {
            version = resolveVersion(versionInput);
        }

        // If Java is outdated, notify the user and give them a chance to exit entirely
        if (JavaVersion.isJavaOutdated()) {
            Cli.printError("Your Java version appears to be outdated (" + System.getProperty("java.version") + "), the lowest supported version is Java 11");
            String response = Cli.prompt("Do you wish to continue anyway? [yn] (n)\n> ");
            if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
                return;
            }
        }

        // If the version the user selected is not installed, install it
        if (!versionManager.installedVersions().contains(version)) {
            Cli.print("@|cyan,bold Version " + version.getIndex() + " not found, installing it|@");
            try {
                versionManager.install(version, status -> {});
            } catch (IOException e) {
                // Exit with an error message if it could not be installed
                throw new ResponseException("Could not install version " + version.getIndex() + ", check your connection");
            }
        }

        // Some versions may require a specific/newer version of the launcher in the future. If the version to be
        // installed is such a version, exit now
        if (!versionManager.isVersionSupported(version)) {
            throw new ResponseException("This launcher does not support version " + version.getIndex());
        }

        // If the EULA has not been accepted, we'll either:
        // * accept it immediately if automatic acceptance was requested on the command line
        // * or, ask the user interactively if they would like to accept it
        if (!versionManager.isEulaAccepted(version)) {
            if (acceptEula) {
                Cli.print("@|cyan,bold Accepting EULA for version " + version.getIndex() + " automatically|@");
                versionManager.acceptEula(version);
            } else {
                Cli.print("The Minecraft EULA has not been accepted for version " + version.getIndex());
                Cli.print("It can be found at @|underline https://account.mojang.com/documents/minecraft_eula|@");

                String response = Cli.prompt("Do you wish to accept it now? [yn] (n)\n> ");
                if (response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes")) {
                    versionManager.acceptEula(version);
                } else {
                    return;
                }
            }
        }

        // Finally, run the server itself in command line mode
        try {
            versionManager.launchCliMode(version, memory);
        } catch (IOException e) {
            // If the server could not start at all (e.g. too much memory allocated), print it here
            e.printStackTrace();
            throw new ResponseException("Could not launch version " + version.getIndex());
        }
    }

    /**
     * Prompts the user for the version they wish to launch.
     */
    private Version queryVersion() {
        // Fetch the latest version so we know what is valid, performs a network request
        Version latestVersion = findLatestVersion();

        // Prompt for the version as a string
        String response = Cli.prompt(String.format(
                "Select a version [1-%s] (latest)\n> ",
                latestVersion.getIndex()
        ));

        // Use the latest version if no reply is given
        if (response.isEmpty()) {
            response = "latest";
        }

        // Parse the input
        Version version = resolveVersion(response);

        // If the version is out of range, throw an error
        if (version.getIndex() > latestVersion.getIndex()) {
            throw new ResponseException(String.format(
                    "Version %s has not been released yet, the latest is %s ",
                    version.getIndex(), latestVersion.getIndex()
            ));
        }

        return version;
    }

    /**
     * Parses a user-provided version string into an actual version.
     *
     * This will perform a network request if the "latest" version is requested.
     */
    private Version resolveVersion(String input) {
        // If latest is requested, determine that version via a network request
        if (input.equals("latest")) {
            return findLatestVersion();
        }

        // Otherwise parse it, throwing an error if the input is invalid
        return Version.parse(input).orElseThrow(() -> new ResponseException("Invalid version"));
    }

    /**
     * Looks up the latest version on GitHub.
     */
    private Version findLatestVersion() {
        try {
            return gitHubAccess.latestRelease();
        } catch (IOException e) {
            throw new ResponseException(
                    "Failed to lookup latest version, check your connection\n" +
                            "You can use an already installed version through the --version option"
            );
        }
    }
}
