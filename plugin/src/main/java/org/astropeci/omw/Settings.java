package org.astropeci.omw;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

@RequiredArgsConstructor
public class Settings {
    public final boolean allowMissileSpawnInEnemyBase;

    @SneakyThrows({ InvalidConfigurationException.class })
    public static Settings fromConfig(FileConfiguration config){
        ConfigurationSection settingsSection = config.getConfigurationSection("settings");
        if(settingsSection == null)
            throw new InvalidConfigurationException("Config has to contain a field called `settings`");

        return new Settings(settingsSection.getBoolean("allowMissileSpawnInEnemyBase", true));
    }


}
