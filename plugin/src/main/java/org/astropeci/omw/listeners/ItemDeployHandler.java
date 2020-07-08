package org.astropeci.omw.listeners;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.astropeci.omw.structures.NoSuchStructureException;
import org.astropeci.omw.structures.Structure;
import org.astropeci.omw.structures.StructureManager;
import org.astropeci.omw.teams.GameTeam;
import org.astropeci.omw.teams.GlobalTeamManager;
import org.astropeci.omw.worlds.ArenaPool;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Level;

import static org.astropeci.omw.ReflectionUtil.fetchClass;

@RequiredArgsConstructor
public class ItemDeployHandler implements Listener {

    private static final float FIREBALL_SIZE = 3.0f;

    private final ArenaPool arenaPool;
    private final StructureManager structureManager;
    private final GlobalTeamManager globalTeamManager;
    private final Plugin plugin;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (arenaPool.getPlayerArena(player).isEmpty()) {
            return;
        }

        Optional<GameTeam> teamOptional = globalTeamManager.getPlayerTeam(player);
        if (teamOptional.isEmpty()) {
            return;
        }

        GameTeam team = teamOptional.get();
        Location target = e.getClickedBlock().getLocation();

        boolean success = deployStructure(e.getMaterial(), team, target.clone()) ||
                deployFireball(e.getMaterial(), target.clone());

        if (success) {
            if (player.getGameMode() == GameMode.SURVIVAL) {
                PlayerInventory inventory = player.getInventory();

                inventory.remove(e.getMaterial());

                if (inventory.getItemInOffHand().getType() == e.getMaterial()) {
                    inventory.setItemInOffHand(new ItemStack(Material.AIR));
                }
            }

            e.setCancelled(true);
        }
    }

    @SneakyThrows({ NoSuchStructureException.class })
    private boolean deployStructure(Material material, GameTeam team, Location target) {
        Structure.Rotation rotation = team == GameTeam.GREEN ?
                Structure.Rotation.ROTATE_180 :
                Structure.Rotation.ROTATE_0;

        String structureName;

        int offsetX;
        int offsetY;
        int offsetZ;

        switch (material) {
            case CREEPER_SPAWN_EGG:
                structureName = "tomahawk";
                offsetX = 0;
                offsetY = 4;
                offsetZ = 4;
                break;
            case GUARDIAN_SPAWN_EGG:
                structureName = "guardian";
                offsetX = 1;
                offsetY = 4;
                offsetZ = 4;
                break;
            case GHAST_SPAWN_EGG:
                structureName = "juggernaut";
                offsetX = 1;
                offsetY = 4;
                offsetZ = 4;
                break;
            case WITCH_SPAWN_EGG:
                structureName = "shieldbuster";
                offsetX = 1;
                offsetY = 4;
                offsetZ = 4;
                break;
            case OCELOT_SPAWN_EGG:
                structureName = "lightning";
                offsetX = 1;
                offsetY = 4;
                offsetZ = 5;
                break;
            default:
                return false;
        }

        Structure structure = new Structure(structureName, structureManager);

        target.setX(target.getX() + (team == GameTeam.GREEN ? 1 : -1) * offsetX);
        target.setY(target.getY() - offsetY);
        target.setZ(target.getZ() + (team == GameTeam.GREEN ? -1 : 1) * offsetZ);

        structure.load(target, team, rotation);

        return true;
    }

    private boolean deployFireball(Material material, Location target) {
        if (material != Material.BLAZE_SPAWN_EGG) {
            return false;
        }

        World world = target.getWorld();
        target.add(0.5, 2, 0.5);

        ArmorStand fireballHolder = (ArmorStand) world.spawnEntity(target, EntityType.ARMOR_STAND);

        fireballHolder.setMarker(true);
        fireballHolder.setGravity(false);
        fireballHolder.setCollidable(false);
        fireballHolder.setVisible(false);

        Fireball fireball = (Fireball) world.spawnEntity(target, EntityType.FIREBALL);
        fireball.setVelocity(new Vector(0, 1, 0));

        // Allow the fireball to move a bit so it blows up if its in some blocks
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (fireball.isDead()) {
                fireballHolder.remove();
            } else {
                fireballHolder.addPassenger(fireball);
            }
        }, 2);

        return true;
    }
}
