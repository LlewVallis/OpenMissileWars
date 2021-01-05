package org.astropeci.omw.settings;

import lombok.Lombok;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;

import java.util.*;

import static org.astropeci.omw.settings.ConfigUtil.*;

@NoArgsConstructor
public class ArenaSettings {

    private Boolean preventSpawningMissilesInEnemyBases = null;
    private Boolean improveExplosionDamageCalculation = null;
    private Boolean preventFireballPushing = null;

    private Map<Material, MissileSpec> missileSpecs = null;
    private Set<ItemSpec> itemSpecs = null;

    @SneakyThrows({ })
    public ArenaSettings(Map<?, ?> map) {
        preventSpawningMissilesInEnemyBases = getBooleanOrNull(map, "preventSpawningMissilesInEnemyBases");
        improveExplosionDamageCalculation = getBooleanOrNull(map, "improveExplosionDamageCalculation");
        preventFireballPushing = getBooleanOrNull(map, "preventFireballPushing");

        Map<?, ?> missilesConfig = getMapOrNull(map, "missiles");
        if (missilesConfig != null) {
            missileSpecs = new HashMap<>();

            missilesConfig.forEach((key, value) -> {
                String keyString = key.toString();

                Material material;
                try {
                    material = Material.valueOf(keyString);
                } catch (IllegalArgumentException e) {
                    throw Lombok.sneakyThrow(new InvalidConfigurationException(key + " is not a valid material", e));
                }

                Map<?, ?> missileConfig = asMap(value, keyString);
                missileSpecs.put(material, new MissileSpec(missileConfig));
            });
        }

        List<?> itemList = getListOrNull(map, "items");
        if (itemList != null) {
            itemSpecs = new HashSet<>();

            for (Object item : itemList) {
                Map<?, ?> itemMap = asMap(item, "items[_]");
                itemSpecs.add(new ItemSpec(itemMap));
            }
        }
    }

    public ArenaSettings merge(ArenaSettings other) {
        ArenaSettings result = new ArenaSettings();

        result.preventSpawningMissilesInEnemyBases = merge(preventSpawningMissilesInEnemyBases, other.preventSpawningMissilesInEnemyBases);
        result.improveExplosionDamageCalculation = merge(improveExplosionDamageCalculation, other.improveExplosionDamageCalculation);
        result.preventFireballPushing = merge(preventFireballPushing, other.preventFireballPushing);

        result.missileSpecs = merge(missileSpecs, other.missileSpecs);
        result.itemSpecs = merge(itemSpecs, other.itemSpecs);

        return result;
    }

    private <T> T merge(T a, T b) {
        return a == null ? b : a;
    }

    public Optional<MissileSpec> getMissileSpec(Material material) {
        if (missileSpecs != null) {
            return Optional.ofNullable(missileSpecs.get(material));
        } else {
            return Optional.empty();
        }
    }

    public Set<ItemSpec> getItemSpecs() {
        if (itemSpecs != null) {
            return Collections.unmodifiableSet(itemSpecs);
        } else {
            return Collections.emptySet();
        }
    }

    public boolean preventSpawningMissilesInEnemyBases() {
        return Objects.requireNonNullElse(preventSpawningMissilesInEnemyBases, false);
    }

    public boolean shouldImproveExplosionDamageCalculation() {
        return Objects.requireNonNullElse(improveExplosionDamageCalculation, false);
    }

    public boolean shouldPreventFireballPushing() {
        return Objects.requireNonNullElse(preventFireballPushing, false);
    }
}
