package org.astropeci.omwlauncher;

import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubAccess {

    private List<GHRelease> releases = null;

    private void init() throws IOException {
        if (releases == null) {
            Cli.printWithoutNewline("Looking up available releases...");

            try {
                GitHub gh = GitHub.connectAnonymously();
                GHRepository repo = gh.getRepository("LlewVallis/OpenMissileWars");

                releases = new ArrayList<>();
                for (GHRelease release : repo.listReleases()) {
                    releases.add(release);
                }
            } finally {
                Cli.print(" (done)");
            }
        }
    }

    public Version latestRelease() throws IOException {
        init();

        String tagName = releases.get(0).getTagName();

        Matcher matcher = Pattern.compile("prebuilt-(\\d+)").matcher(tagName);
        if (!matcher.matches()) {
            throw new IllegalStateException("Invalid tag name: " + tagName);
        }

        String versionString = matcher.group(1);
        int versionIndex = Integer.parseInt(versionString);
        return new Version(versionIndex);
    }

    public String releaseDescription(Version version) throws IOException {
        init();

        return releases.stream()
                .filter(release -> release.getTagName().equals("prebuilt-" + version.getIndex()))
                .findFirst()
                .map(GHRelease::getBody)
                .orElse("");
    }
}
