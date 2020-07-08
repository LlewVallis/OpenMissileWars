package org.astropeci.omw;

import org.bukkit.Bukkit;

public class ReflectionUtil {

    private static String serverVersionCache = null;

    public static Class<?> fetchClass(String nameTemplate) throws ClassNotFoundException {
        String name = nameTemplate.replace("$OBC", "org.bukkit.craftbukkit.$VER")
                .replace("$NMS", "net.minecraft.server.$VER")
                .replace("$VER", getServerVersion());

        return Class.forName(name);
    }

    private static String getServerVersion() {
        if (serverVersionCache == null) {
            serverVersionCache = Bukkit.getServer().getClass().getPackageName().split("\\.")[3];
            Bukkit.getLogger().info("Detected server version as " + serverVersionCache);
        }

        return serverVersionCache;
    }
}
