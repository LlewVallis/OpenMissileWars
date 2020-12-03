package org.astropeci.omwlauncher;

import lombok.Value;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.function.Consumer;

public class VersionSelectionWidget extends JPanel {

    private final JComboBox<WrappedVersion> comboBox;
    private final List<Version> versions;
    private final List<Version> installedVersions;

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

        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                WrappedVersion wrapped = (WrappedVersion) e.getItem();
                onVersionChange.accept(wrapped.getVersion());
            }
        });

        add(comboBox);
    }

    public void refresh() {
        int index = comboBox.getSelectedIndex();
        comboBox.setModel(createModel());
        comboBox.setSelectedIndex(index);
    }

    private ComboBoxModel<WrappedVersion> createModel() {
        WrappedVersion[] wrappedVersions = versions.stream()
                .map(version -> new WrappedVersion(version, installedVersions.contains(version)))
                .toArray(WrappedVersion[]::new);

        return new DefaultComboBoxModel<>(wrappedVersions);
    }
}
