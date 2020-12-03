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

@RequiredArgsConstructor
public class VersionManager {

    private final List<Version> installedVersions = new ArrayList<>();
    private final Path dataPath = Paths.get("omw-installations");
    { refreshInstalledVersions(); }

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

        Process process = new ProcessBuilder(baseServerArgs(memory, serverDirectory))
                .directory(serverDirectory.toFile())
                .start();

        Thread thread = new Thread(() -> {
            Scanner scanner = new Scanner(process.getInputStream());

            while (true) {
                String line;
                try {
                    line = scanner.nextLine();
                } catch (NoSuchElementException e) {
                    onCrash.run();
                    break;
                }

                Cli.printWithoutNewline("@|cyan,bold [PaperMC] |@");
                System.out.println(line);

                if (line.contains("Starting minecraft server")) {
                    onStartup.run();
                    break;
                }
            }
        });

        thread.start();
    }

    @SneakyThrows({ InterruptedException.class })
    public void launchCliMode(Version version, int memory) throws IOException {
        Path serverDirectory = serverDirectory(version);

        Cli.print("\n@|blue <========== |@@|green,bold,underline Open|@@|red,bold,underline MissileWars|@@|blue  ==========>|@");

        List<String> args = baseServerArgs(memory, serverDirectory);
        args.add("nogui");

        Process process = new ProcessBuilder(args)
                .directory(serverDirectory.toFile())
                .inheritIO()
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                process.destroy();
                if (!process.waitFor(90L, TimeUnit.SECONDS)) {
                    Cli.print("Server refusing to exit, killing it");
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                throw Lombok.sneakyThrow(e);
            }
        }));

        process.waitFor();
    }

    private List<String> baseServerArgs(int memory, Path serverDirectory) {
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

        if (Files.isRegularFile(serverDirectory.resolve("agent.jar"))) {
            args.add(1, "-javaagent:agent.jar");
        }

        return args;
    }

    private Path serverDirectory(Version version) {
        Path installationDirectory = installationDirectory(version);
        return installationDirectory.resolve("server");
    }

    public void install(Version version, Consumer<Double> listener) throws IOException {
        URL url = new URL(String.format(
                "https://github.com/LlewVallis/OpenMissileWars/releases/download/prebuilt-%s/server.tar.gz",
                version.getIndex()
        ));

        Path destDirectory = installationDirectory(version);

        Cli.print("Extracting @|underline " + url + "|@ to @|underline " + destDirectory.toAbsolutePath() + "|@");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        long contentLength = connection.getContentLengthLong();

        try (ProgressBar progressBar = createProgressBar(contentLength)) {
            @Cleanup TarArchiveInputStream input = new TarArchiveInputStream(
                    new GZIPInputStream(
                            new ProgressInputStream(
                                    connection.getInputStream(),
                                    count -> {
                                        listener.accept((double) count / contentLength);
                                        progressBar.stepTo(count);
                                    }
                            )
                    )
            );

            extractTar(input, destDirectory);
            progressBar.stepTo(contentLength);
        }

        Cli.print("@|green,bold Finished installation|@");
        listener.accept(1d);
        refreshInstalledVersions();
    }

    private ProgressBar createProgressBar(long contentLength) {
        return new ProgressBar(
                "Downloading",
                contentLength,
                250,
                System.out,
                ProgressBarStyle.ASCII,
                "MiB",
                1024 * 1024,
                true,
                new DecimalFormat("0.0"),
                ChronoUnit.SECONDS,
                0L,
                Duration.ZERO
        );
    }

    private void extractTar(TarArchiveInputStream input, Path destDirectory) throws IOException {
        TarArchiveEntry entry;
        while ((entry = input.getNextTarEntry()) != null) {
            Path dest = destDirectory.resolve(entry.getName());

            if (!dest.startsWith(destDirectory)) {
                Cli.printError("Refusing to expand out of bounds file: " + dest);
                continue;
            }

            if (entry.isDirectory()) {
                Files.createDirectories(dest);
            } else {
                Files.createDirectories(dest.getParent());
                Files.copy(input, dest);
            }
        }
    }

    public void uninstall(Version version) throws IOException {
        Path installationDirectory = installationDirectory(version);

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

    @SneakyThrows({ IOException.class })
    public void acceptEula(Version version) {
        Path eulaFile = serverDirectory(version).resolve("eula.txt");
        Files.write(eulaFile, "eula=true".getBytes());
    }

    public boolean isVersionSupported(Version version) {
        Path launcherSettingsFile = serverDirectory(version).resolve("launcher-settings.json");
        return !Files.exists(launcherSettingsFile);
    }

    @SneakyThrows({ IOException.class })
    private void refreshInstalledVersions() {
        installedVersions.clear();

        Files.createDirectories(dataPath);
        Files.list(dataPath)
                .flatMap(path -> {
                    String fileName = path.getFileName().toString();
                    Matcher matcher = Pattern.compile("version-(\\d+)").matcher(fileName);

                    if (!matcher.matches()) {
                        return Stream.empty();
                    }

                    String versionString = matcher.group(1);
                    try {
                        int versionIndex = Integer.parseInt(versionString);
                        if (versionIndex > 0) {
                            return Stream.of(new Version(versionIndex));
                        }
                    } catch (NumberFormatException ignored) { }

                    return Stream.empty();
                })
                .sorted(Comparator.comparingInt(Version::getIndex).reversed())
                .forEach(installedVersions::add);
    }

    private Path installationDirectory(Version version) {
        return dataPath.resolve("version-" + version.getIndex());
    }

    public List<Version> installedVersions() {
        return Collections.unmodifiableList(installedVersions);
    }
}
