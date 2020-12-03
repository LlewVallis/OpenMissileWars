package org.astropeci.omwlauncher;

import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
public class CliFlow {

    private final String versionInput;
    private final boolean acceptEula;
    private final int memory;

    private final GitHubAccess gitHubAccess = new GitHubAccess();
    private final VersionManager versionManager = new VersionManager();

    public void run() {
        Version version;
        if (versionInput.isEmpty()) {
            version = queryVersion();
        } else {
            version = resolveVersion(versionInput);
        }

        if (JavaVersion.isJavaOutdated()) {
            Cli.printError("Your Java version appears to be outdated (" + System.getProperty("java.version") + "), the lowest supported version is Java 11");
            String response = Cli.prompt("Do you wish to continue anyway? [yn] (n)\n> ");
            if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
                return;
            }
        }

        if (!versionManager.installedVersions().contains(version)) {
            Cli.print("@|cyan,bold Version " + version.getIndex() + " not found, installing it|@");
            try {
                versionManager.install(version, status -> {});
            } catch (IOException e) {
                throw new ResponseException("Could not install version " + version.getIndex() + ", check your connection");
            }
        }

        if (!versionManager.isVersionSupported(version)) {
            throw new ResponseException("This launcher does not support version " + version.getIndex());
        }

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

        try {
            versionManager.launchCliMode(version, memory);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseException("Could not launch version " + version.getIndex());
        }
    }

    private Version queryVersion() {
        Version latestVersion = findLatestVersion();

        String response = Cli.prompt(String.format(
                "Select a version [1-%s] (latest)\n> ",
                latestVersion.getIndex()
        ));

        if (response.isEmpty()) {
            response = "latest";
        }

        Version version = resolveVersion(response);

        if (version.getIndex() > latestVersion.getIndex()) {
            throw new ResponseException(String.format(
                    "Version %s has not been released yet, the latest is %s ",
                    version.getIndex(), latestVersion.getIndex()
            ));
        }

        return version;
    }

    private Version resolveVersion(String input) {
        if (input.equals("latest")) {
            return findLatestVersion();
        }

        return Version.parse(input).orElseThrow(() -> new ResponseException("Invalid version"));
    }

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
