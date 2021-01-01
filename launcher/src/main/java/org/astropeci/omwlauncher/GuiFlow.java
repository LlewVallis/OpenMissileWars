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

/**
 * Manages the lifecycle of the launcher when it is used in GUI mode.
 *
 * Unlike the CLI launcher, the GUI launcher closes when a successful server startup has been detected. The started
 * server opens its own GUI, making the launcher one superfluous.
 */
public class GuiFlow {

    // This is a large, messy class and could use some refactoring at some point. It does work for now though

    private final GitHubAccess gitHubAccess = new GitHubAccess();
    private final VersionManager versionManager = new VersionManager();

    /**
     * A latch which is counted down when the GUI flow should terminate.
     *
     * This is needed since the program is otherwise idle listening for Swing events.
     */
    private final CountDownLatch exitLatch = new CountDownLatch(1);

    /**
     * A list of all installed versions used for indications in the UI.
     *
     * This list is updated as versions are installed and uninstalled.
     */
    private final List<Version> installedVersions = versionManager.installedVersions();

    /**
     * The currently selected version.
     */
    private Version version;

    /**
     * True if the EULA has been accepted via the CLI.
     */
    private final boolean acceptEula;

    /**
     * The window for the launcher.
     */
    private final JFrame frame;

    /**
     * The widget which allows selecting a version from a dropdown list.
     */
    private VersionSelectionWidget selectionWidget;

    /**
     * Whether the buttons have been created yet.
     *
     * This is useful to know since the {@link #onBaseVersionChange(Version)} method must update the buttons, but is
     * also fired once during initialization before the buttons have been created.
     */
    private boolean buttonsSetup = false;

    /**
     * The button to launch the selected version.
     */
    private JButton launchButton;

    /**
     * The button to install the selected version.
     */
    private JButton installButton;

    /**
     * The input box for selecting the amount of memory to allocate in GiB.
     */
    private JSpinner memorySpinner;

    public GuiFlow(String versionInput, boolean acceptEula, int memory) {
        this.acceptEula = acceptEula;

        frame = new JFrame("OpenMissileWars Launcher");

        // Setup a panel which will contain all the elements of the GUI
        JPanel panel = new JPanel();
        // Layout elements vertically top-to-bottom
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        // Use a blank margin of 20px
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Setup the contents of the GUI
        addVersionWidget(panel, versionInput);
        addMemorySpinner(panel, memory);
        addButtons(panel);

        frame.add(panel);

        // Change the close operation on the window such that the main thread is unblocked and then goes on to terminate
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

    /**
     * Open the graphical interface and wait for the user to close the launcher
     */
    @SneakyThrows({ InterruptedException.class })
    public void run() {
        // Center the GUI, open it and then wait for it to close
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        exitLatch.await();
    }

    /**
     * Add the version dropdown box to the GUI.
     */
    private void addVersionWidget(Container container, String versionInput) {
        // If a version was specified on the command line, and it was not "latest" (which is ignored), then we'll add a
        // special fixed version selection widget which is locked into the specified version
        if (!versionInput.equals("latest") && !versionInput.isEmpty()) {
            // Attempt to parse the version into a nullable variable
            Version version = Version.parse(versionInput).orElse(null);

            // Print an error and continue if it was invalid, otherwise add the aforementioned fixed version widget
            if (version == null) {
                Cli.printError("Invalid version, ignoring");
            } else {
                addFixedVersionWidget(container, version);
                return;
            }
        }

        // Fetch all available versions with a network request
        Optional<List<Version>> versionsOptional = fetchVersions();
        if (versionsOptional.isPresent()) {
            // If the request succeeded, display the versions in a normal dropdown
            addNormalVersionWidget(container, versionsOptional.get());
        } else if (installedVersions.isEmpty()) {
            // If it failed and no versions are installed, display a widget telling the user to check their connection
            addNoVersionsWidget(container);
        } else {
            // If it failed but some versions are installed, show only those ones
            addInstalledVersionsWidget(container, new ArrayList<>(installedVersions));
        }
    }

    /**
     * Adds normal the version dropdown box with all available versions up for selection.
     */
    private void addNormalVersionWidget(Container container, List<Version> versions) {
        // Create a large, uneditable text box for displaying release notes for the selected version
        JTextArea versionDescription = new JTextArea(7, 40);
        versionDescription.setLineWrap(true);
        versionDescription.setWrapStyleWord(true);
        versionDescription.setEditable(false);
        versionDescription.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Fire the version change code with the selected (aka latest) version. This updates parts of the GUI which
        // depend on the currently selected version
        Version defaultVersion = versions.get(0);
        onNormalVersionChange(defaultVersion, versionDescription);

        selectionWidget = new VersionSelectionWidget(
                versions,
                installedVersions,
                // Not locked - the version can be switched freely
                false,
                // When the buttons change, update the GUI and currently selected version
                version -> onNormalVersionChange(version, versionDescription)
        );

        container.add(selectionWidget);
        container.add(new JScrollPane(versionDescription));
    }

    /**
     * Updates the GUI for a version change assuming the normal online-only version description box is present.
     */
    @SneakyThrows({ IOException.class })
    private void onNormalVersionChange(Version version, JTextArea versionDescription) {
        // Update the description here since its online only, then run the base GUI changes which fire in all
        // situations. Note that this does not trigger a network request since releases have been cached
        versionDescription.setText(gitHubAccess.releaseDescription(version));
        onBaseVersionChange(version);
    }

    /**
     * Updates the GUI and internal state when the version changes in all situations.
     */
    private void onBaseVersionChange(Version version) {
        this.version = version;
        // Since this can be called during initialization, the buttons might not exist yet
        if (buttonsSetup) configureButtons();
    }

    /**
     * Adds a message saying that the releases could not be fetched and none were installed; hence making the launcher
     * unusable.
     */
    private void addNoVersionsWidget(Container container) {
        // Wrap the labels in a grid to get them centered and stacked on top of one another
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(new JLabel("Could not fetch versions, and none are installed", SwingConstants.CENTER));
        panel.add(new JLabel("Check your connection and try again", SwingConstants.CENTER));
        container.add(panel);
    }

    /**
     * Adds the limited installed version only selection widget accompanied by a message saying releases could not be
     * fetched.
     */
    private void addInstalledVersionsWidget(Container container, List<Version> versions) {
        // Trigger the base version change directly. There is no release description box to update, so a "normal version
        // update" is not applicable
        Version defaultVersion = versions.get(0);
        onBaseVersionChange(defaultVersion);

        selectionWidget = new VersionSelectionWidget(
                // All the installed versions, this will be a copy of the next parameter
                versions,
                installedVersions,
                // Not locked - the version can be switched freely
                false,
                // When the buttons change, update the GUI and currently selected version
                this::onBaseVersionChange
        );

        container.add(selectionWidget);

        // Add the message saying releases could not be fetched. The labels are wrapped in a grid so they are centered
        // and stacked on top of one another
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(new JLabel("Could not fetch versions, only installed ones are available", SwingConstants.CENTER));
        panel.add(new JLabel("Check your connection and try again", SwingConstants.CENTER));
        container.add(panel);
    }

    /**
     * Adds the locked version widget for when the version is specified via the command line.
     */
    private void addFixedVersionWidget(Container container, Version version) {
        selectionWidget = new VersionSelectionWidget(
                // All the versions in the pseudo-dropdown, of which there is one
                Collections.singletonList(version),
                installedVersions,
                // Locked - the dropdown cannot be used (its aesthetic only)
                true,
                // The version cannot change, so no need to handle it
                _version -> {}
        );

        // Directly set the current version
        this.version = version;
        container.add(selectionWidget);
    }

    /**
     * Adds the input box for specifying the memory to be allocated.
     */
    private void addMemorySpinner(Container container, int memory) {
        // Use an embedded panel to put the label and spinner box on the same line
        JPanel panel = new JPanel();
        panel.add(new JLabel("Server memory (GiB, fractions allowed)"));
        memorySpinner = new JSpinner(new SpinnerNumberModel(
                // The starting memory value converted to GiB since memory is internally stored in MiB
                memory / 1024d,
                // The smallest amount of memory allowed (256 MiB)
                0.25,
                // The largest amount of memory allowed (1 TiB)
                1024,
                // Step by a GiB every time the spinner arrows are clicked
                1
        ));
        panel.add(memorySpinner);

        container.add(panel);
    }

    /**
     * Adds the launch, install/uninstall and submit issue buttons.
     */
    private void addButtons(Container container) {
        // Use an embedded panel to put all the buttons on the same line
        JPanel panel = new JPanel();

        if (version != null) {
            // Button labels are updated depending on the selected version, so the labels are initialized later with the
            // same code
            launchButton = new JButton();
            installButton = new JButton();

            launchButton.addActionListener(e -> launch());
            installButton.addActionListener(e -> installOrUninstall());

            // Initializes the labels for the buttons depending on the version
            configureButtons();

            panel.add(launchButton);
            panel.add(installButton);
        }

        // Add the static submit issue button
        JButton submitIssueButton = new JButton("Submit issue");
        submitIssueButton.addActionListener(e -> submitIssue());
        panel.add(submitIssueButton);

        // Mark the buttons as initialized, allowing them to be modified from version changes
        buttonsSetup = true;
        container.add(panel);
    }

    /**
     * Sets the descriptions for the action buttons based on the currently selected version.
     */
    private void configureButtons() {
        if (installedVersions.contains(version)) {
            // If the version is installed
            launchButton.setText("Launch");
            installButton.setText("Uninstall");
        } else {
            // If it needs to be installed
            launchButton.setText("Install and launch");
            installButton.setText("Install");
        }
    }

    /**
     * Opens the web browser to the GitHub issue page.
     */
    private void submitIssue() {
        // This won't work in some environments, so errors are displayed on the console if it fails
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

    /**
     * Shows a popup dialog informing the user that their Java version needs to be updated.
     * @return True if they wish to ignore the warning, false if they want to abort launching.
     */
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

        // Only continue if "Yes" is selected, closing the dialog, clicking "No" etc will cancel
        return response == JOptionPane.YES_OPTION;
    }

    /**
     * Shows a popup dialog informing the user that the launcher does not support the selected version.
     *
     * This does not ask if they wish to continue since that is forbidden (cancelling the operation is forced).
     */
    private void promptVersionUnsupported() {
        JOptionPane.showMessageDialog(
                null,
                "This launcher does not support version " + version.getIndex(),
                "Outdated launcher",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Shows a popup dialog asking if the user wishes to accept the Minecraft EULA.
     * @return True if they wish to accept, false if they want to abort launching.
     */
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

        // Only continue if "Yes" is selected, closing the dialog, clicking "No" etc will cancel
        return response == JOptionPane.YES_OPTION;
    }

    /**
     * Constructs, but doesn't show, a dialog informing the user that OpenMissileWars is starting up.
     */
    private JDialog createLaunchingDialog() {
        // By attaching to the frame and disabling closing of the dialog illegal operations such as launching a version
        // twice is prevented
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

    /**
     * Attempts to launch the currently selected version, prompting the user on any issues that arise.
     */
    private void launch() {
        // If Java is outdated and the user does not wish to continue, abort
        if (JavaVersion.isJavaOutdated() && !promptJavaOutdated()) {
            return;
        }

        // If the version is not installed, install it
        if (!installedVersions.contains(version)) {
            install();
        }

        // If the version is not supported by this launcher, prompt the user and abort
        if (!versionManager.isVersionSupported(version)) {
            promptVersionUnsupported();
            return;
        }

        // If the Minecraft EULA has not been accepted for this version then either
        // * accept it immediately if automatic acceptance was requested on the command line
        // * or, prompt the user asking if they would like to accept it
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

        // Construct (but don't show) the launching dialog
        JDialog dialog = createLaunchingDialog();
        // Pull the requested amount of allocated memory from the spinner, converting it from GiB to MiB
        int memory = (int) (((double) memorySpinner.getValue()) * 1024);

        try {
            // Launch the server asynchronously using GUI mode
            versionManager.launchGuiMode(
                    version, memory,
                    // Handle the server starting successfully by making a note on console, closing the launch dialog
                    // and then exiting the launcher entirely
                    () -> {
                        SwingUtilities.invokeLater(() -> {
                            Cli.print("Server started, exiting launcher");
                            dialog.dispose();
                            exitLatch.countDown();
                        });
                    },
                    // Handle the server failing to start by closing the launch dialog, showing the user an error
                    // message and then exiting the launcher when it is acknowledged. This happens when the server
                    // process can be created, but the server itself does properly initialize
                    () -> {
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
                    }
            );
        } catch (IOException e) {
            // Print an error and notify the user if the server process could not be created at all. This is relatively
            // rare
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Could not launch version " + version.getIndex() + " since the server process could not " +
                            "be created",
                    "Error launching",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        // After the server has been started asynchronously, show the launching dialog
        dialog.setVisible(true);
    }

    /**
     * Toggles whether the currently selected version is installed.
     */
    private void installOrUninstall() {
        if (installedVersions.contains(version)) {
            uninstall();
        } else {
            install();
        }
    }

    /**
     * Installs the currently selected version.
     */
    private void install() {
        // A dialog tracking the installation progress
        JDialog dialog = new JDialog(frame, "Installation", true);

        // A two lines are used - one for the "Installing version" heading and one for the progress bar
        JPanel panel = new JPanel(new GridLayout(2, 1));
        // Add the heading
        panel.add(new JLabel("Installing version " + version.getIndex(), SwingConstants.CENTER));
        // Add the progress bar, measuring progress from 0 (no progress) to 1000 (completed)
        JProgressBar progressBar = new JProgressBar(0, 1000);
        panel.add(progressBar);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setResizable(false);

        // Start installing the version asynchronously so it does not block the AWT event thread needed for updating the
        // GUI
        CompletableFuture.runAsync(() -> {
            try {
                try {
                    versionManager.install(version, status -> {
                        // Update the progress bar's progress out of 1000
                        progressBar.setValue((int) (status * 1000));
                    });
                } finally {
                    // When done, close the dialog and refresh the GUI so the installation status can be reflected. The
                    // list of installed versions is updated for us by the version manager
                    SwingUtilities.invokeLater(() -> {
                        dialog.dispose();
                        selectionWidget.refresh();
                        configureButtons();
                    });
                }
            } catch (IOException e) {
                // If there was an error, print it to console and show it in an error box
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

    /**
     * Uninstalls the currently selected version.
     */
    private void uninstall() {
        // Prompt for confirmation
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
                // If there was an error print to console and prompt
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        frame,
                        "Could not uninstall version " + version.getIndex(),
                        "Error uninstalling",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

        // Refresh the GUI. The installed versions list is updated for us by the version manager
        selectionWidget.refresh();
        configureButtons();
    }

    /**
     * Attempt to fetch all available versions using a network request, without throwing on an error.
     * @return A list of available versions if they could be fetched, otherwise an empty optional.
     */
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
