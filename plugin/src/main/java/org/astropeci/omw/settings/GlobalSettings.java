package org.astropeci.omw.settings;

import lombok.SneakyThrows;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.astropeci.omw.settings.ConfigUtil.*;

public class GlobalSettings {

    private final Set<String> startingArenas;
    private final Map<String, ArenaSettings> arenaConfigs;

    @SneakyThrows({ InvalidConfigurationException.class })
    public GlobalSettings(FileConfiguration config) {
        try {
            Map<?, ?> configMap = config.getValues(false);

            startingArenas = new HashSet<>();
            for (Object element : getList(configMap, "arenas")) {
                String elementString = asString(element, "arenas[_]");

                if (startingArenas.contains(elementString)) {
                    throw new InvalidConfigurationException("duplicate element in arenas");
                }

                startingArenas.add(elementString);
            }

            arenaConfigs = new LinkedHashMap<>();
            for (Object arenaConfigObject : getList(configMap, "configs")) {
                Map<?, ?> arenaConfig = asMap(arenaConfigObject, "configs[_]");

                String regex = getString(arenaConfig, "pattern");
                try {
                    compileRegex(regex);
                } catch (PatternSyntaxException e) {
                    throw new InvalidConfigurationException(regex + " is not a valid regular expression", e);
                }

                arenaConfigs.put(regex, new ArenaSettings(arenaConfig));
            }
        } catch (InvalidConfigurationException e) {
            throw new InvalidConfigurationException("Invalid config.yml: " + e.getMessage(), e);
        }
    }

    public Set<String> getStartingArenas() {
        return Collections.unmodifiableSet(startingArenas);
    }

    public ArenaSettings createSettings(String arenaName) {
        ArenaSettings result = new ArenaSettings();

        for (Map.Entry<String, ArenaSettings> entry : arenaConfigs.entrySet()) {
            Pattern pattern = compileRegex(entry.getKey());
            Matcher matcher = pattern.matcher(arenaName);

            if (matcher.matches()) {
                result = result.merge(entry.getValue());
            }
        }

        return result;
    }

    private Pattern compileRegex(String key) {
        return Pattern.compile(key);
    }
}
