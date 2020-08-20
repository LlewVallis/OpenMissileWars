package org.astropeci.omw.listeners;

import lombok.RequiredArgsConstructor;
import org.astropeci.omw.worlds.NamedArena;
import org.astropeci.omw.teams.GameTeam;
import org.astropeci.omw.worlds.ArenaPool;
import org.astropeci.omw.worlds.NoSuchArenaException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

@RequiredArgsConstructor
public class PortalBreakListener implements Listener {

    private final ArenaPool arenaPool;
    private final Plugin plugin;

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent e) {
        Location location = e.getLocation();

        boolean portalDestroyed = e.blockList().stream()
                .anyMatch(block -> block.getType() == Material.NETHER_PORTAL);

        if (!portalDestroyed) {
            return;
        }

        GameTeam team;
        if (location.getZ() < 0) {
            team = GameTeam.GREEN;
        } else {
            team = GameTeam.RED;
        }

        World world = location.getWorld();
        Optional<NamedArena> arenaOptional = arenaPool.getArenaForWorld(world);

        arenaOptional.ifPresent(arena -> {
            boolean wasRunning = arena.arena.processWinner(team);

            if (wasRunning) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    try {
                        arenaPool.resetArena(arena.name);
                    } catch (NoSuchArenaException ex) {
                        throw new IllegalStateException(ex);
                    }
                }, 125);
            }
        });
    }
}
