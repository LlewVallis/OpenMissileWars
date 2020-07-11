package org.astropeci.omw.listeners;

import lombok.RequiredArgsConstructor;
import org.astropeci.omw.teams.GlobalTeamManager;
import org.astropeci.omw.worlds.ArenaPool;
import org.astropeci.omw.worlds.Hub;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

@RequiredArgsConstructor
public class SpawnHandler implements Listener {

    private final Hub hub;
    private final ArenaPool arenaPool;
    private final GlobalTeamManager globalTeamManager;

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        hub.sendPlayer(e.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();

        World world = player.getWorld();

        Location worldSpawn = world.getSpawnLocation();
        worldSpawn.setX(worldSpawn.getX() + 0.5);
        worldSpawn.setZ(worldSpawn.getZ() + 0.5);
        worldSpawn.setYaw(-90);

        Location spawnPoint = arenaPool.getPlayerArena(player)
                .flatMap(arena -> globalTeamManager.getPlayerTeam(player)
                        .map(arena.arena::getSpawn))
                .orElse(worldSpawn);

        e.setRespawnLocation(spawnPoint);
    }
}
