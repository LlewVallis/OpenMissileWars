package org.astropeci.omw.worlds;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.FileUtil;
import org.astropeci.omw.teams.GameTeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

@RequiredArgsConstructor
public class Arena implements AutoCloseable {

    private final World world;
    private final WorldManager worldManager;
    private final Hub hub;

    public void sendPlayerToLobby(Player player) {
        worldManager.send(player, world);
    }

    public boolean hasPlayer(Player player) {
        return player.getWorld().equals(world);
    }

    @Override
    public void close() {
        for (Player player : world.getPlayers()) {
            TextComponent message = new TextComponent("Closing arena");
            message.setColor(ChatColor.GREEN);
            player.spigot().sendMessage(message);

            hub.sendPlayer(player);
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
                FileUtil.deleteRecursive(worldDirectory.toPath());
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to clean world files for " + world.getName(), e);
            }
        } else {
            Bukkit.getLogger().warning("Could not clean world files for " + world.getName() + " as they did not exist");
        }
    }

    public Location getSpawn(GameTeam team) {
        switch (team) {
            case GREEN:
                return new Location(world, 123.5, 76, 65.5, 180, 0);
            case RED:
                return new Location(world, 123.5, 76, -64.5, 0, 0);
            default:
                throw new IllegalStateException();
        }
    }
}
