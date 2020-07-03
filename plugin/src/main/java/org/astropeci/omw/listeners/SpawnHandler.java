package org.astropeci.omw.listeners;

import org.astropeci.omw.worlds.Hub;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class SpawnHandler implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Hub.sendPlayer(e.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        World world = e.getPlayer().getWorld();

        Location spawnPoint = world.getSpawnLocation();
        Location alignedSpawnPoint = new Location(world, spawnPoint.getX() + 0.5, spawnPoint.getY(), spawnPoint.getZ() + 0.5);

        e.setRespawnLocation(alignedSpawnPoint);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        e.getEntity().spigot().respawn();
    }
}
