package org.astropeci.omw.worlds;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.Util;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class Arena implements AutoCloseable {

    private final World world;

    public Arena(World world) {
        this.world = world;
    }

    public static Arena create() {
        String worldName = "omw-arena-" + UUID.randomUUID();
        Bukkit.getLogger().info("Creating arena under directory " + worldName);

        World world = Template.createWorld(worldName);
        return new Arena(world);
    }

    public void sendPlayer(Player player) {
        Worlds.send(player, world);
    }

    @Override
    public void close() {
        for (Player player : world.getPlayers()) {
            TextComponent message = new TextComponent("Closing arena");
            message.setColor(ChatColor.GREEN);
            player.spigot().sendMessage(message);

            Hub.sendPlayer(player);
        }

        Bukkit.getLogger().info("Unloading arena " + world.getName());
        boolean success = Bukkit.unloadWorld(world, false);

        if (!success) {
            Bukkit.getLogger().warning("Could not unload " + world.getName());
            return;
        }

        File worldDirectory = world.getWorldFolder();

        if (worldDirectory.exists()) {
            try {
                Util.deleteRecursive(worldDirectory.toPath());
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to clean world files for " + world.getName(), e);
            }
        } else {
            Bukkit.getLogger().warning("Could not clean world files for " + world.getName() + " as they did not exist");
        }
    }
}
