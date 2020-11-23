package org.astropeci.omw;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

@RequiredArgsConstructor
public class Settings {

    @Getter
    private final boolean allowSpawningMissilesInEnemyBases;

    @SneakyThrows({ InvalidConfigurationException.class })
    public static Settings fromConfig(FileConfiguration config) {
        ConfigurationSection settingsSection = config.getConfigurationSection("settings");
        if (settingsSection == null) {
            throw new InvalidConfigurationException("config should contain a field named 'settings'");
        }

        return new Settings(
                settingsSection.getBoolean("allowSpawningMissilesInEnemyBases", true)
        );
    }
}
