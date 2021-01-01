package org.astropeci.omwlauncher;

import lombok.Value;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * A Swing component for displaying available versions and allowing the user to select from them.
 *
 * The component is able to invoke a callback when the user selects a different version.
 */
public class VersionSelectionWidget extends JPanel {

    /**
     * The underlying element used to render the widget.
     */
    private final JComboBox<WrappedVersion> comboBox;

    /**
     * A list of all versions that exist and can be selected from.
     */
    private final List<Version> versions;

    /**
     * An updating list of all versions which are installed.
     */
    private final List<Version> installedVersions;

    /**
     * A wrapper around a version and its installation status with a {@link #toString()} method suitable for displaying
     * in {@link #comboBox}.
     */
    @Value
    private static class WrappedVersion {
        Version version;
        boolean installed;

        @Override
        public String toString() {
            String base = "Version " + version.getIndex();
            if (installed) {
                return base + " (installed)";
            } else {
                return base;
            }
        }
    }

    /**
     * Constructs a new version widget.
     *
     * @param versions A list of all available versions.
     * @param installedVersions A list of all installed versions. This should update any installations/uninstallations
     *                          that occur.
     * @param locked Whether to allow the user to change the selected version.
     * @param onVersionChange Called when the user selects a different version.
     */
    public VersionSelectionWidget(
            List<Version> versions,
            List<Version> installedVersions,
            boolean locked,
            Consumer<Version> onVersionChange
    ) {
        this.versions = versions;
        this.installedVersions = installedVersions;

        add(new JLabel("Select a version"));

        comboBox = new JComboBox<>(createModel());
        comboBox.setEnabled(!locked);

        // Listen for item selection events
        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                // Unwrap the selected version and notify the listener
                WrappedVersion wrapped = (WrappedVersion) e.getItem();
                onVersionChange.accept(wrapped.getVersion());
            }
        });

        add(comboBox);
    }

    /**
     * Refreshes the component to reflect changes in the installed versions.
     */
    public void refresh() {
        // Gets the selected item, resets the storage model and then sets the selected item back to what it was
        int index = comboBox.getSelectedIndex();
        comboBox.setModel(createModel());
        comboBox.setSelectedIndex(index);
    }

    /**
     * Creates a model for storing versions in the combox box.
     */
    private ComboBoxModel<WrappedVersion> createModel() {
        // A list of all available wrapped versions
        WrappedVersion[] wrappedVersions = versions.stream()
                .map(version -> new WrappedVersion(version, installedVersions.contains(version)))
                .toArray(WrappedVersion[]::new);

        return new DefaultComboBoxModel<>(wrappedVersions);
    }
}
