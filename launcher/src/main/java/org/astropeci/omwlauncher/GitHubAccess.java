package org.astropeci.omwlauncher;

import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides access to release information on the OpenMissileWars GitHub repository.
 *
 * This class performs a network request the first time release information is accessed, and then caches it.
 */
public class GitHubAccess {

    /**
     * A cache of the releases on https://github.com/LlewVallis/OpenMissileWars.
     */
    private List<GHRelease> releases = null;

    /**
     * Initializes the release cache if it is uninitialized.
     */
    private void init() throws IOException {
        if (releases == null) {
            // Since this could block for some time, make a note of the action. The "done indicator" is printed onto the
            // same line
            Cli.printWithoutNewline("Looking up available releases...");

            try {
                // Connect to the API and fetch the repository
                GitHub gh = GitHub.connectAnonymously();
                GHRepository repo = gh.getRepository("LlewVallis/OpenMissileWars");

                // Populate the cache with the release information
                releases = new ArrayList<>();
                for (GHRelease release : repo.listReleases()) {
                    releases.add(release);
                }
            } finally {
                Cli.print(" (done)");
            }
        }
    }

    /**
     * Finds the latest release, may perform a network request.
     */
    public Version latestRelease() throws IOException {
        init();

        String tagName = releases.get(0).getTagName();

        // All release tag names are in the format prebuilt-N where N is the release number. Its fine to crash if a
        // release is found outside this pattern since that's not allowed
        Matcher matcher = Pattern.compile("prebuilt-(\\d+)").matcher(tagName);
        if (!matcher.matches()) {
            throw new IllegalStateException("Invalid tag name: " + tagName);
        }

        // Fetches the (\\d+) portion of the regex and parses it into a version
        String versionString = matcher.group(1);
        int versionIndex = Integer.parseInt(versionString);
        return new Version(versionIndex);
    }

    /**
     * Finds the description/changlog of a release, may perform a network request.
     *
     * If a version does not exist, an empty string is returned.
     */
    public String releaseDescription(Version version) throws IOException {
        init();

        return releases.stream()
                .filter(release -> release.getTagName().equals("prebuilt-" + version.getIndex()))
                .findFirst()
                .map(GHRelease::getBody)
                .orElse("");
    }
}
