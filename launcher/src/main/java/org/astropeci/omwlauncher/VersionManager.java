package org.astropeci.omwlauncher;

import lombok.Cleanup;
import lombok.Lombok;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * Handles installing and launching versions as well as managing already installed versions.
 */
@RequiredArgsConstructor
public class VersionManager {

    /**
     * A continually up to date (and hence mutating) list of which versions are installed, sorted from latest to oldest.
     */
    private final List<Version> installedVersions = new ArrayList<>();

    /**
     * The path to the version installation directory.
     */
    private final Path dataPath = Paths.get("omw-installations");

    // Determine which versions are installed upon initialization
    { refreshInstalledVersions(); }

    /**
     * Launches a version in GUI mode.
     *
     * Launching OpenMissileWars in GUI mode upholds the following properties:
     * <ul>
     *     <li>The server process is able to run after the launcher process dies</li>
     *     <li>The launch itself is non-blocking</li>
     *     <li>Server output is copied onto the console until the server has fully started</li>
     *     <li>The server's itself is run with the GUI enabled</li>
     * </ul>
     *
     * There are three possible outcomes from this operation:
     * <ul>
     *     <li>The server starts successfully and {@code onStartup} is called</li>
     *     <li>The server process starts properly but crashes and {@code onCrash} is called</li>
     *     <li>The server process could not start and an {@link IOException} is thrown</li>
     * </ul>
     */
    public void launchGuiMode(
            Version version,
            int memory,
            Runnable onStartup,
            Runnable onCrash
    ) throws IOException {
        Path serverDirectory = serverDirectory(version);

        Cli.print(String.format(
                "@|cyan,bold Launching OpenMissileWars version %s with %sMiB of memory in GUI mode|@",
                version.getIndex(), memory
        ));

        // Create the process itself. No action needs to be taken to make the server GUI appear; that is the default
        // behavior
        Process process = new ProcessBuilder(baseServerArgs(memory, serverDirectory))
                .directory(serverDirectory.toFile())
                .start();

        // Launching is non-blocking, so we'll need a separate thread to copy the console output over
        Thread thread = new Thread(() -> {
            Scanner scanner = new Scanner(process.getInputStream());

            // Read and print each line of output in a loop
            while (true) {
                String line;
                try {
                    line = scanner.nextLine();
                } catch (NoSuchElementException e) {
                    // If the process closes (and therefore no output is available) then mark it as crashed
                    onCrash.run();
                    break;
                }

                // Print a "[PaperMC]" prefix before printing out the line
                Cli.printWithoutNewline("@|cyan,bold [PaperMC] |@");
                System.out.println(line);

                // This line is usually output as the server has successfully launched, so we'll treat this as our
                // success condition. In GUI mode we don't copy more output beyond this line since it all goes into a
                // graphical console created by Paper anyway
                if (line.contains("Starting minecraft server")) {
                    onStartup.run();
                    break;
                }
            }
        });

        // Launch the output copying thread async
        thread.start();
    }

    /**
     * Launches a version in CLI mode.
     *
     * Launching OpenMissileWars in CLI mode upholds the following properties:
     * <ul>
     *     <li>
     *         A best effort is made to prevent the server being able to continue after the launcher dies, even in
     *         extreme cases
     *     </li>
     *     <li>The launch itself is blocking and effectively takes the launcher process over</li>
     *     <li>The server's IO streams are mapped 1:1 to the launcher's</li>
     *     <li>The server's itself is run with the GUI disabled</li>
     * </ul>
     *
     * There are two possible outcomes from this operation:
     * <ul>
     *     <li>The server process starts and this method returns normally when it ends</li>
     *     <li>The server process could not start and an {@link IOException} is thrown</li>
     * </ul>
     */
    @SneakyThrows({ InterruptedException.class })
    public void launchCliMode(Version version, int memory) throws IOException {
        Path serverDirectory = serverDirectory(version);

        // Print a fancy header to signify that the server process has taken over
        Cli.print("\n@|blue <========== |@@|green,bold,underline Open|@@|red,bold,underline MissileWars|@@|blue  ==========>|@");

        // Append "nogui" to the server arguments so the Paper GUI is not shown
        List<String> args = baseServerArgs(memory, serverDirectory);
        args.add("nogui");

        // Start the server itself, mapping its IO directly to the launcher
        Process process = new ProcessBuilder(args)
                .directory(serverDirectory.toFile())
                .inheritIO()
                .start();

        // Add a shutdown hook to prevent Paper running if the launcher dies
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // Ask nicely for it to exit, then kill it after 90 seconds. This is somewhat on a deadline since the
                // launcher itself ought to exit ASAP when its process has been closed
                process.destroy();
                if (!process.waitFor(90L, TimeUnit.SECONDS)) {
                    Cli.print("Server refusing to exit, killing it");
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                throw Lombok.sneakyThrow(e);
            }
        }));

        // Block until the server dies
        process.waitFor();
    }

    /**
     * Returns a set of arguments, including the "java" command, which will launch the server properly.
     */
    private List<String> baseServerArgs(int memory, Path serverDirectory) {
        // Invocation of paper.jar with some optimizations and tunings enabled
        List<String> args = new ArrayList<>(Arrays.asList(
                "java",
                "-Xms" + memory + "M", "-Xmx" + memory + "M", "-XX:+UseG1GC", "-XX:+ParallelRefProcEnabled",
                "-XX:MaxGCPauseMillis=200", "-XX:+UnlockExperimentalVMOptions", "-XX:+DisableExplicitGC",
                "-XX:+AlwaysPreTouch", "-XX:G1NewSizePercent=30", "-XX:G1MaxNewSizePercent=40",
                "-XX:G1HeapRegionSize=8M", "-XX:G1ReservePercent=20", "-XX:G1HeapWastePercent=5",
                "-XX:G1MixedGCCountTarget=4", "-XX:InitiatingHeapOccupancyPercent=15",
                "-XX:G1MixedGCLiveThresholdPercent=90", "-XX:G1RSetUpdatingPauseTimePercent=5",
                "-XX:SurvivorRatio=32", "-XX:+PerfDisableSharedMem", "-XX:MaxTenuringThreshold=1",
                "-Dusing.aikars.flags=https://mcflags.emc.gs", "-Daikars.new.flags=true",
                "-jar", "paper.jar"
        ));

        // Modern versions of OpenMissileWars use an agent.jar which must be added to the argument list. Unfortunately,
        // some old versions did not, so whether or not the argument is required must be detected based on the presence
        // of the agent file
        if (Files.isRegularFile(serverDirectory.resolve("agent.jar"))) {
            args.add(1, "-javaagent:agent.jar");
        }

        return args;
    }

    /**
     * Returns the server installation directory for a version.
     */
    private Path serverDirectory(Version version) {
        Path installationDirectory = installationDirectory(version);
        return installationDirectory.resolve("server");
    }

    /**
     * Downloads and installs a version into the {@code ./omw-installations} directory.
     * @param listener A callback which is frequently updated with the progress of the operation represented as a number
     *                 between 0 and 1 inclusive.
     * @throws IOException if the version could not be installed
     */
    public void install(Version version, Consumer<Double> listener) throws IOException {
        // This method downloads the server files from GitHub. Whilst the launcher makes an effort not to allow anything
        // obviously wrong to happen other than an error if the files are malformed, they are still ultimately a trusted
        // source of information which could cause havoc if compromised. Https is of course used to make sure they
        // cannot be compromised outside of a takeover of the GitHub repository or a very major security flaw elsewhere

        // The URL of the prebuilt server to download
        URL url = new URL(String.format(
                "https://github.com/LlewVallis/OpenMissileWars/releases/download/prebuilt-%s/server.tar.gz",
                version.getIndex()
        ));

        Path destDirectory = installationDirectory(version);
        Cli.print("Extracting @|underline " + url + "|@ to @|underline " + destDirectory.toAbsolutePath() + "|@");

        // Connect to the server and determine the size of the server so relative/overall progress can be determined
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        long contentLength = connection.getContentLengthLong();

        // Displays the console progress bar within the try block
        try (ProgressBar progressBar = createProgressBar(contentLength)) {
            // Interpret the download as a .tar.gz input stream
            @Cleanup TarArchiveInputStream input = new TarArchiveInputStream(
                    new GZIPInputStream(
                            new ProgressInputStream(
                                    connection.getInputStream(),
                                    count -> {
                                        // Call the listener and update the console progress bar periodically
                                        listener.accept((double) count / contentLength);
                                        progressBar.stepTo(count);
                                    }
                            )
                    )
            );

            // Extract the files from the stream, calling the listener/progress bar updating code periodically. Note
            // that there is no distinct downloading and extracting phase, the extraction works directly off the URL
            // stream
            extractTar(input, destDirectory);
            // Clean up the progress bar to show it completed entirely
            progressBar.stepTo(contentLength);
        }

        Cli.print("@|green,bold Finished installation|@");
        listener.accept(1d);
        refreshInstalledVersions();
    }

    /**
     * Creates a {@link ProgressBar} for representing installation progress on the command line.
     */
    private ProgressBar createProgressBar(long contentLength) {
        return new ProgressBar(
                // The name of the task
                "Downloading",
                // The total amount of bytes being downloaded
                contentLength,
                // The update interval
                250,
                // Print the progress bar to System.out
                System.out,
                // Use the ASCII (instead of Unicode) mode since that works better on more consoles
                ProgressBarStyle.ASCII,
                // The unit to use, using bytes directly would not be very readable because of the large numbers
                "MiB",
                // The size of a MiB
                1024 * 1024,
                // Show the speed
                true,
                // Show speed with one decimal place
                new DecimalFormat("0.0"),
                // Use MiB per second as the speed unit
                ChronoUnit.SECONDS,
                // Start with no time invested and no progress made
                0L,
                Duration.ZERO
        );
    }

    /**
     * Extracts a {@link TarArchiveInputStream} into a directory.
     *
     * This method does not require the archive to be already downloaded if it is supplied from a network request, it
     * can download and extract concurrently from the stream.
     * @throws IOException if the extraction failed
     */
    private void extractTar(TarArchiveInputStream input, Path destDirectory) throws IOException {
        // Iterate over each file in the archive until there are no more
        TarArchiveEntry entry;
        while ((entry = input.getNextTarEntry()) != null) {
            // Find the location within the installation directory for the file
            Path dest = destDirectory.resolve(entry.getName());

            // Should never be needed, but if a file somehow ends up needing to be placed outside the installation
            // directory we'll refuse to do so
            if (!dest.startsWith(destDirectory)) {
                Cli.printError("Refusing to expand out of bounds file: " + dest);
                continue;
            }

            // Create a file or directory depending on what is required
            if (entry.isDirectory()) {
                Files.createDirectories(dest);
            } else {
                // The parent directories *should* be spelt out in the archive and therefore already created, but just
                // in case we'll create the required parent directories if they are missing. This is a no-op if they do
                // already exist as they should
                Files.createDirectories(dest.getParent());
                // And then of course copy the file out of the stream. This might seem strange since it seems like the
                // entire stream is being copied, but the stream "ends" wherever the current file does and then restarts
                // when the next entry is requested
                Files.copy(input, dest);
            }
        }
    }

    /**
     * Uninstalls a version from the {@code ./omw-installations} directory.
     * @throws IOException if the operation failed
     */
    public void uninstall(Version version) throws IOException {
        Path installationDirectory = installationDirectory(version);

        // Recursive deletion of installationDirectory
        Files.walkFileTree(installationDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });

        refreshInstalledVersions();
    }

    /**
     * Determines whether the Minecraft EULA has been accepted for the specified version.
     *
     * This works by scanning the {@code eula.txt} file in the version's installation files.
     */
    @SneakyThrows({ IOException.class })
    public boolean isEulaAccepted(Version version) {
        Path eulaFile = serverDirectory(version).resolve("eula.txt");

        if (!Files.isRegularFile(eulaFile)) {
            return false;
        }

        return Files.readAllLines(eulaFile).stream()
                .map(String::trim)
                .anyMatch(line -> line.equalsIgnoreCase("eula=true"));
    }

    /**
     * Accepts the Minecraft EULA for the specified version.
     *
     * This writes {@code eula=true} to the {@code eula.txt} file in the version's installation files.
     */
    @SneakyThrows({ IOException.class })
    public void acceptEula(Version version) {
        Path eulaFile = serverDirectory(version).resolve("eula.txt");
        Files.write(eulaFile, "eula=true".getBytes());
    }

    /**
     * Determines whether the specified version is supported by the launcher. This only works if the version is
     * installed.
     *
     * Currently all versions of OpenMissileWars are supported by the launcher, but in case a future version needs to
     * mandate the use of a newer launcher it can add a {@code launcher-settings.json} file to its server files. If such
     * a file is detected, this method returns {@code false} and the launcher will refuse to run the version.
     */
    public boolean isVersionSupported(Version version) {
        Path launcherSettingsFile = serverDirectory(version).resolve("launcher-settings.json");
        return !Files.exists(launcherSettingsFile);
    }

    /**
     * Rebuilds the list of installed versions based on the {@code ./omw-installations} directory.
     */
    @SneakyThrows({ IOException.class })
    private void refreshInstalledVersions() {
        // We're completely recreating the list of installed versions, so wipe the old one
        installedVersions.clear();

        // The installation directory is required for this, so create it if it does not exist
        Files.createDirectories(dataPath);

        // Iterate over every file in the installation directory
        Files.list(dataPath)
                .flatMap(path -> {
                    // Attempt to match the file name to the expected format
                    String fileName = path.getFileName().toString();
                    Matcher matcher = Pattern.compile("version-(\\d+)").matcher(fileName);

                    // If it does not follow the version-N format, discard that entry
                    if (!matcher.matches()) {
                        return Stream.empty();
                    }

                    // Extracts the number component of the matched file name
                    String versionString = matcher.group(1);
                    try {
                        // Parse the version number, if it is a valid version then use it
                        int versionIndex = Integer.parseInt(versionString);
                        if (versionIndex > 0) {
                            return Stream.of(new Version(versionIndex));
                        }
                    } catch (NumberFormatException ignored) { }

                    // If it is not a valid version, discard it
                    return Stream.empty();
                })
                // Sort the versions from newest to oldest
                .sorted(Comparator.comparingInt(Version::getIndex).reversed())
                // And add them to the list of installed versions
                .forEach(installedVersions::add);
    }

    /**
     * The directory the files for an installed version should live in.
     */
    private Path installationDirectory(Version version) {
        return dataPath.resolve("version-" + version.getIndex());
    }

    /**
     * An updating list of all installed versions sorted from newest to oldest.
     */
    public List<Version> installedVersions() {
        return Collections.unmodifiableList(installedVersions);
    }
}
