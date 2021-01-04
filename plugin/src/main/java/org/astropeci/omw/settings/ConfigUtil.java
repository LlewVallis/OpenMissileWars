package org.astropeci.omw.settings;

import lombok.SneakyThrows;
import org.bukkit.configuration.InvalidConfigurationException;

import java.util.List;
import java.util.Map;

// We cant use @UtilityClass because it doesn't work well with static imports
public class ConfigUtil {

    public static List<?> getListOrNull(Map<?, ?> map, String key) {
        Object object = map.get(key);
        return asListOrNull(object, key);
    }

    public static List<?> asListOrNull(Object object, String key) {
        return object == null ? null : asList(object, key);
    }

    public static List<?> getList(Map<?, ?> map, String key) {
        Object object = map.get(key);
        return asList(object, key);
    }

    @SneakyThrows({ InvalidConfigurationException.class })
    public static List<?> asList(Object object, String key) {
        if (!(object instanceof List)) {
            throw new InvalidConfigurationException(key + " is missing or not a list");
        }

        return (List<?>) object;
    }

    public static Map<?, ?> getMapOrNull(Map<?, ?> map, String key) {
        Object object = map.get(key);
        return asMapOrNull(object, key);
    }

    public static Map<?, ?> asMapOrNull(Object object, String key) {
        return object == null ? null : asMap(object, key);
    }

    public static Map<?, ?> getMap(Map<?, ?> map, String key) {
        Object object = map.get(key);
        return asMap(object, key);
    }

    @SneakyThrows({ InvalidConfigurationException.class })
    public static Map<?, ?> asMap(Object object, String key) {
        if (!(object instanceof Map)) {
            throw new InvalidConfigurationException(key + " is missing or not a map");
        }

        return (Map<?, ?>) object;
    }

    public static String getStringOrNull(Map<?, ?> map, String key) {
        Object object = map.get(key);
        return asStringOrNull(object, key);
    }

    public static String asStringOrNull(Object object, String key) {
        return object == null ? null : asString(object, key);
    }

    public static String getString(Map<?, ?> map, String key) {
        Object object = map.get(key);
        return asString(object, key);
    }

    @SneakyThrows({ InvalidConfigurationException.class })
    public static String asString(Object object, String key) {
        if (!(object instanceof String)) {
            throw new InvalidConfigurationException(key + " is missing or not a string");
        }

        return (String) object;
    }

    public static Boolean getBooleanOrNull(Map<?, ?> map, String key) {
        Object object = map.get(key);
        return asBooleanOrNull(object, key);
    }

    public static Boolean asBooleanOrNull(Object object, String key) {
        return object == null ? null : asBoolean(object, key);
    }

    @SneakyThrows({ InvalidConfigurationException.class })
    public static boolean asBoolean(Object object, String key) {
        if (!(object instanceof Boolean)) {
            throw new InvalidConfigurationException(key + " is missing or not a boolean");
        }

        return (Boolean) object;
    }

    public static Integer getIntOrNull(Map<?, ?> map, String key) {
        Object object = map.get(key);
        return asIntOrNull(object, key);
    }

    public static Integer asIntOrNull(Object object, String key) {
        return object == null ? null : asInt(object, key);
    }

    public static int getInt(Map<?, ?> map, String key) {
        Object object = map.get(key);
        return asInt(object, key);
    }

    @SneakyThrows({ InvalidConfigurationException.class })
    public static int asInt(Object object, String key) {
        if (!(object instanceof Integer)) {
            throw new InvalidConfigurationException(key + " is missing or not an integer");
        }

        return (Integer) object;
    }
}
