package org.astropeci.omw.settings;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;

import java.util.Map;
import java.util.Objects;

import static org.astropeci.omw.settings.ConfigUtil.*;

@Getter
public class ItemSpec {

    private final Material material;
    private final String name;
    private final int amount;

    @SneakyThrows({ InvalidConfigurationException.class })
    public ItemSpec(Map<?, ?> itemMap) {
        String materialString = getString(itemMap, "material");

        try {
            material = Material.valueOf(materialString);
        } catch (IllegalArgumentException e) {
            throw new InvalidConfigurationException(materialString + " is not a valid material", e);
        }

        name = getStringOrNull(itemMap, "name");
        amount = Objects.requireNonNullElse(getIntOrNull(itemMap, "amount"), 1);
    }
}
