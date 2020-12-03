package org.astropeci.omwlauncher;

import lombok.SneakyThrows;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class GuiFlow {

    private final GitHubAccess gitHubAccess = new GitHubAccess();
    private final VersionManager versionManager = new VersionManager();
    private final CountDownLatch exitLatch = new CountDownLatch(1);
    private final List<Version> installedVersions = versionManager.installedVersions();

    private Version version;
    private final boolean acceptEula;

    private final JFrame frame;
    private VersionSelectionWidget selectionWidget;
    private boolean buttonsSetup = false;
    private JButton launchButton;
    private JButton installButton;
    private JSpinner memorySpinner;

    public GuiFlow(String versionInput, boolean acceptEula, int memory) {
        this.acceptEula = acceptEula;

        frame = new JFrame("OpenMissileWars Launcher");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        addVersionWidget(panel, versionInput);
        addMemorySpinner(panel, memory);
        addButtons(panel);

        frame.add(panel);

        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitLatch.countDown();
            }
        });

        frame.pack();
        frame.setResizable(false);
    }

    @SneakyThrows({ InterruptedException.class })
    public void run() {
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        exitLatch.await();
    }

    private void addVersionWidget(Container container, String versionInput) {
        if (!versionInput.equals("latest") && !versionInput.isEmpty()) {
            Version version = Version.parse(versionInput).orElse(null);

            if (version == null) {
                Cli.printError("Invalid version, ignoring");
            } else {
                addFixedVersionWidget(container, version);
                return;
            }
        }

        Optional<List<Version>> versionsOptional = fetchVersions();
        if (versionsOptional.isPresent()) {
            addNormalVersionWidget(container, versionsOptional.get());
        } else if (installedVersions.isEmpty()) {
            addNoVersionsWidget(container);
        } else {
            addInstalledVersionsWidget(container, new ArrayList<>(installedVersions));
        }
    }

    private void addNormalVersionWidget(Container container, List<Version> versions) {
        JTextArea versionDescription = new JTextArea(7, 40);
        versionDescription.setLineWrap(true);
        versionDescription.setWrapStyleWord(true);
        versionDescription.setEditable(false);
        versionDescription.setBorder(new EmptyBorder(10, 10, 10, 10));

        Version defaultVersion = versions.get(0);
        onNormalVersionChange(defaultVersion, versionDescription);

        selectionWidget = new VersionSelectionWidget(
                versions,
                installedVersions,
                false,
                version -> onNormalVersionChange(version, versionDescription)
        );

        container.add(selectionWidget);
        container.add(new JScrollPane(versionDescription));
    }

    @SneakyThrows({ IOException.class })
    private void onNormalVersionChange(Version version, JTextArea versionDescription) {
        versionDescription.setText(gitHubAccess.releaseDescription(version));
        onBaseVersionChange(version);
    }

    private void onBaseVersionChange(Version version) {
        this.version = version;
        if (buttonsSetup) configureButtons();
    }

    private void addNoVersionsWidget(Container container) {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(new JLabel("Could not fetch versions, and none are installed", SwingConstants.CENTER));
        panel.add(new JLabel("Check your connection and try again", SwingConstants.CENTER));
        container.add(panel);
    }

    private void addInstalledVersionsWidget(Container container, List<Version> versions) {
        Version defaultVersion = versions.get(0);
        onBaseVersionChange(defaultVersion);

        selectionWidget = new VersionSelectionWidget(
                versions,
                installedVersions,
                false,
                this::onBaseVersionChange
        );

        container.add(selectionWidget);

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(new JLabel("Could not fetch versions, only installed ones are available", SwingConstants.CENTER));
        panel.add(new JLabel("Check your connection and try again", SwingConstants.CENTER));
        container.add(panel);
    }

    private void addFixedVersionWidget(Container container, Version version) {
        selectionWidget = new VersionSelectionWidget(
                Collections.singletonList(version),
                installedVersions,
                true,
                _version -> {}
        );

        this.version = version;
        container.add(selectionWidget);
    }

    private void addMemorySpinner(Container container, int memory) {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Server memory (GiB, fractions allowed)"));
        memorySpinner = new JSpinner(new SpinnerNumberModel(memory / 1024d, 0.25, 1024, 1));
        panel.add(memorySpinner);
        container.add(panel);
    }

    private void addButtons(Container container) {
        JPanel panel = new JPanel();

        if (version != null) {
            launchButton = new JButton();
            installButton = new JButton();

            launchButton.addActionListener(e -> launch());
            installButton.addActionListener(e -> installOrUninstall());
            configureButtons();

            panel.add(launchButton);
            panel.add(installButton);
        }

        JButton submitIssueButton = new JButton("Submit issue");
        submitIssueButton.addActionListener(e -> submitIssue());
        panel.add(submitIssueButton);

        buttonsSetup = true;
        container.add(panel);
    }

    private void configureButtons() {
        if (installedVersions.contains(version)) {
            launchButton.setText("Launch");
            installButton.setText("Uninstall");
        } else {
            launchButton.setText("Install and launch");
            installButton.setText("Install");
        }
    }

    private void submitIssue() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URI.create("https://github.com/LlewVallis/OpenMissileWars/issues/new"));
            } catch (IOException e) {
                Cli.printError("Could not open browser");
            }
        } else {
            Cli.printError("Cannot submit issue opening the browser is not supported");
        }
    }

    private boolean promptJavaOutdated() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Your Java version appears to be outdated (" + System.getProperty("java.version") + ")"));
        panel.add(new JLabel("The lowest supported version is Java 11"));
        panel.add(new JLabel("Do you wish to continue anyway?"));

        int response = JOptionPane.showConfirmDialog(
                frame,
                panel,
                "Outdated Java",
                JOptionPane.YES_NO_OPTION
        );

        return response == JOptionPane.YES_OPTION;
    }

    private void promptVersionUnsupported() {
        JOptionPane.showMessageDialog(
                null,
                "This launcher does not support version " + version.getIndex(),
                "Outdated launcher",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private boolean promptEulaNotAccepted() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("The Minecraft EULA has not been accepted for version " + version.getIndex()));
        panel.add(new JLabel("It can be found at https://account.mojang.com/documents/minecraft_eula"));
        panel.add(new JLabel("Do you wish to accept it now?"));

        int response = JOptionPane.showConfirmDialog(
                frame,
                panel,
                "Minecraft EULA",
                JOptionPane.YES_NO_OPTION
        );

        return response == JOptionPane.YES_OPTION;
    }

    private JDialog createLaunchingDialog() {
        JDialog dialog = new JDialog(frame);
        JPanel panel = new JPanel();

        panel.add(new JLabel("Starting OpenMissileWars version " + version.getIndex()));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setResizable(false);

        return dialog;
    }

    private void launch() {
        if (JavaVersion.isJavaOutdated() && !promptJavaOutdated()) {
            return;
        }

        if (!installedVersions.contains(version)) {
            install();
        }

        if (!versionManager.isVersionSupported(version)) {
            promptVersionUnsupported();
            return;
        }

        if (!versionManager.isEulaAccepted(version)) {
            if (acceptEula) {
                Cli.print("@|cyan,bold Accepting EULA for version " + version.getIndex() + " automatically|@");
                versionManager.acceptEula(version);
            } else if (promptEulaNotAccepted()) {
                versionManager.acceptEula(version);
            } else {
                return;
            }
        }

        JDialog dialog = createLaunchingDialog();
        int memory = (int) (((double) memorySpinner.getValue()) * 1024);

        try {
            versionManager.launchGuiMode(version, memory, () -> {
                SwingUtilities.invokeLater(() -> {
                    Cli.print("Server started, exiting launcher");
                    dialog.dispose();
                    exitLatch.countDown();
                });
            }, () -> {
                SwingUtilities.invokeLater(() -> {
                    dialog.dispose();
                    JOptionPane.showMessageDialog(
                            null,
                            "Could not launch version " + version.getIndex() + " since the server crashed " +
                                    "while starting up",
                            "Error launching",
                            JOptionPane.ERROR_MESSAGE
                    );
                    exitLatch.countDown();
                });
            });
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Could not launch version " + version.getIndex() + " since the server process could not " +
                            "be created",
                    "Error launching",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        dialog.setVisible(true);
    }

    private void installOrUninstall() {
        if (installedVersions.contains(version)) {
            uninstall();
        } else {
            install();
        }
    }

    private void install() {
        JDialog dialog = new JDialog(frame, "Installation", true);

        JPanel panel = new JPanel(new GridLayout(2, 1));

        panel.add(new JLabel("Installing version " + version.getIndex(), SwingConstants.CENTER));
        JProgressBar progressBar = new JProgressBar(0, 1000);
        panel.add(progressBar);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setResizable(false);

        CompletableFuture.runAsync(() -> {
            try {
                try {
                    versionManager.install(version, status -> {
                        progressBar.setValue((int) (status * 1000));
                    });
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        dialog.dispose();
                        selectionWidget.refresh();
                        configureButtons();
                    });
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            frame,
                            "Could not install version " + version.getIndex() + ", check your connection",
                            "Error installing",
                            JOptionPane.ERROR_MESSAGE
                    );
                });
            }
        });

        dialog.setVisible(true);
    }

    private void uninstall() {
        int confirmationStatus = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to uninstall version " + version.getIndex() + "?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmationStatus == JOptionPane.YES_OPTION) {
            try {
                versionManager.uninstall(version);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        frame,
                        "Could not uninstall version " + version.getIndex(),
                        "Error uninstalling",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

        selectionWidget.refresh();
        configureButtons();
    }

    private Optional<List<Version>> fetchVersions() {
        try {
            Version latest = gitHubAccess.latestRelease();

            List<Version> versions = new ArrayList<>();
            for (int i = latest.getIndex(); i > 0; i--) {
                versions.add(new Version(i));
            }

            return Optional.of(versions);
        } catch (IOException e) {
            Cli.printError("Failed to lookup latest version, check your connection");
            return Optional.empty();
        }
    }
}
