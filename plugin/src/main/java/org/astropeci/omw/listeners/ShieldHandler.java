package org.astropeci.omw.listeners;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.astropeci.omw.structures.NoSuchStructureException;
import org.astropeci.omw.structures.Structure;
import org.astropeci.omw.structures.StructureManager;
import org.astropeci.omw.teams.GameTeam;
import org.astropeci.omw.teams.GlobalTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class ShieldHandler implements Listener {

    private static final int SHIELD_DELAY = 20;

    private final StructureManager structureManager;
    private final GlobalTeamManager globalTeamManager;
    private final Plugin plugin;

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity() instanceof Snowball) {
            Snowball snowball = (Snowball) e.getEntity();
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> shieldifySnowball(snowball), SHIELD_DELAY);
        }
    }

    @SneakyThrows({ NoSuchStructureException.class })
    private void shieldifySnowball(Snowball snowball) {
        if (snowball.isDead()) {
            return;
        }

        GameTeam team;
        if (snowball.getShooter() instanceof Player) {
            team = globalTeamManager.getPlayerTeam((Player) snowball.getShooter())
                    .orElse(GameTeam.GREEN);
        } else {
            team = GameTeam.GREEN;
        }

        Location target = snowball.getLocation().toBlockLocation();
        target.subtract(3, 3, 0);

        Structure structure = new Structure("shield", structureManager);
        structure.load(target, team, Structure.Rotation.ROTATE_0);

        snowball.remove();
    }
}
