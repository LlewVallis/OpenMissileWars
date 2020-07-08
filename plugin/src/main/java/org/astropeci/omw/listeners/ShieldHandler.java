package org.astropeci.omw.listeners;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.structures.NoSuchStructureException;
import org.astropeci.omw.structures.Structure;
import org.astropeci.omw.structures.StructureManager;
import org.astropeci.omw.teams.GameTeam;
import org.astropeci.omw.teams.GlobalTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;

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

        ProjectileSource shooter = snowball.getShooter();

        GameTeam team;
        if (shooter instanceof Player) {
            team = globalTeamManager.getPlayerTeam((Player) snowball.getShooter())
                    .orElse(GameTeam.GREEN);
        } else {
            team = GameTeam.GREEN;
        }

        Location target = snowball.getLocation().toBlockLocation();
        target.subtract(3, 3, 0);

        World world = snowball.getWorld();

        for (int x = target.getBlockX(); x < target.getX() + 7; x++) {
            for (int y = target.getBlockY(); y < target.getY() + 7; y++) {
                Block block = world.getBlockAt(x, y, target.getBlockZ());

                if (block.getType() == Material.OBSIDIAN || block.getType() == Material.NETHER_PORTAL) {
                    if (shooter instanceof Entity) {
                        TextComponent message = new TextComponent("A shild cannot be spawned there");
                        message.setColor(ChatColor.RED);
                        ((Entity) shooter).sendMessage(message);
                    }

                    return;
                }
            }
        }

        Structure structure = new Structure("shield", structureManager);
        structure.load(target, team, Structure.Rotation.ROTATE_0);

        snowball.remove();
    }
}
