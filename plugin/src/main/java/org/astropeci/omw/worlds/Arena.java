package org.astropeci.omw.worlds;

import com.destroystokyo.paper.Title;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.FileUtil;
import org.astropeci.omw.item.PeriodicItemDispenser;
import org.astropeci.omw.teams.GameTeam;
import org.astropeci.omw.teams.GlobalTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;

public class Arena implements AutoCloseable {

    private final World world;
    private final WorldManager worldManager;
    private final Hub hub;

    private final PeriodicItemDispenser dispenser;

    private boolean ended = false;

    public Arena(World world, GlobalTeamManager globalTeamManager, WorldManager worldManager, Hub hub, Plugin plugin) {
        dispenser = new PeriodicItemDispenser(world, globalTeamManager, plugin);

        this.world = world;
        this.worldManager = worldManager;
        this.hub = hub;

        dispenser.start();
    }

    public void sendPlayerToLobby(Player player) {
        worldManager.send(player, world);
    }

    public boolean isAttachedToWorld(World world) {
        return world.equals(this.world);
    }

    @Override
    public void close() {
        dispenser.close();

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
                return new Location(world, 123.5, 77, 65.5, 180, 0);
            case RED:
                return new Location(world, 123.5, 77, -64.5, 0, 0);
            default:
                throw new IllegalStateException();
        }
    }

    public boolean processWinner(Optional<GameTeam> teamOptional) {
        if (ended) {
            return false;
        }

        String teamName = teamOptional.map(team -> team == GameTeam.GREEN ? "Green" : "Red").orElse("Diffy");
        ChatColor color = teamOptional.map(team -> team == GameTeam.GREEN ? ChatColor.GREEN : ChatColor.RED).orElse(ChatColor.BLUE);

        TextComponent titleMessage = new TextComponent(teamName + " wins the game");
        titleMessage.setColor(color);
        Title title = new Title(titleMessage, null, 5, 80, 40);

        world.getPlayers().forEach(player -> {
            player.sendTitle(title);
            player.setGameMode(GameMode.SPECTATOR);
        });

        ended = true;
        return true;
    }
}
