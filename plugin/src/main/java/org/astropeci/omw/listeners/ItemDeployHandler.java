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

import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class ItemDeployHandler implements Listener {

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

        boolean success = deployStructure(team, target.clone(), e) ||
                deployFireball(e.getMaterial(), target.clone());

        if (success) {
            if (player.getGameMode() == GameMode.SURVIVAL) {
                Material material = e.getMaterial();
                PlayerInventory inventory = player.getInventory();

                if (inventory.getItemInMainHand().getType() == material) {
                    int amount = inventory.getItemInMainHand().getAmount();
                    if (amount == 1) {
                        inventory.setItemInMainHand(new ItemStack(Material.AIR));
                    } else {
                        inventory.setItemInMainHand(new ItemStack(material, amount - 1));
                    }
                } else {
                    int amount = inventory.getItemInOffHand().getAmount();
                    if (amount == 1) {
                        inventory.setItemInOffHand(new ItemStack(Material.AIR));
                    } else {
                        inventory.setItemInOffHand(new ItemStack(material, amount - 1));
                    }
                }
            }

            e.setCancelled(true);
        }
    }

    @SneakyThrows({ NoSuchStructureException.class })
    private boolean deployStructure(GameTeam team, Location target, PlayerInteractEvent e) {
        Material material = e.getMaterial();
        Player player = e.getPlayer();

        Structure.Rotation rotation = team == GameTeam.GREEN ?
                Structure.Rotation.ROTATE_180 :
                Structure.Rotation.ROTATE_0;

        String structureName;

        int offsetX;
        int offsetY;
        int offsetZ;

        int width;
        int height;
        int length;

        switch (material) {
            case CREEPER_SPAWN_EGG:
                structureName = "tomahawk";

                offsetX = 0;
                offsetY = 4;
                offsetZ = 4;

                width = 2;
                height = 2;
                length = 13;
                break;
            case GUARDIAN_SPAWN_EGG:
                structureName = "guardian";

                offsetX = 1;
                offsetY = 4;
                offsetZ = 4;

                width = 3;
                height = 3;
                length = 8;
                break;
            case GHAST_SPAWN_EGG:
                structureName = "juggernaut";

                offsetX = 1;
                offsetY = 4;
                offsetZ = 4;

                width = 3;
                height = 3;
                length = 11;
                break;
            case WITCH_SPAWN_EGG:
                structureName = "shieldbuster";

                offsetX = 1;
                offsetY = 4;
                offsetZ = 4;

                width = 3;
                height = 3;
                length = 15;
                break;
            case OCELOT_SPAWN_EGG:
                structureName = "lightning";

                offsetX = 1;
                offsetY = 4;
                offsetZ = 5;

                width = 3;
                height = 2;
                length = 9;
                break;
            default:
                return false;
        }

        target.setX(target.getX() + (team == GameTeam.GREEN ? 1 : -1) * offsetX);
        target.setY(target.getY() - offsetY);
        target.setZ(target.getZ() + (team == GameTeam.GREEN ? -1 : 1) * offsetZ);

        boolean canSpawn = canSpawn(team, target, width, height, length);

        if (!canSpawn) {
            TextComponent message = new TextComponent("You cannot place a " + structureName + " there");
            message.setColor(ChatColor.RED);
            player.spigot().sendMessage(message);

            // Cancel without succeeding
            e.setCancelled(true);
            return false;
        }

        Structure structure = new Structure(structureName, structureManager);
        structure.load(target, team, rotation);

        return true;
    }

    private boolean canSpawn(GameTeam team, Location location, int width, int height, int length) {
        World world = location.getWorld();

        int teamDirectionMultiplier = team == GameTeam.GREEN ? -1 : 1;

        int startX = location.getBlockX();
        int startY = location.getBlockY();
        int startZ = location.getBlockZ();

        Set<Material> friendlyBlocks = Set.of(
                team == GameTeam.GREEN ? Material.GREEN_STAINED_GLASS : Material.RED_STAINED_GLASS,
                team == GameTeam.GREEN ? Material.LIME_STAINED_GLASS : Material.PINK_STAINED_GLASS
        );

        Set<Material> enemyBlocks = Set.of(
                team == GameTeam.GREEN ? Material.RED_STAINED_GLASS : Material.GREEN_STAINED_GLASS,
                team == GameTeam.GREEN ? Material.PINK_STAINED_GLASS : Material.LIME_STAINED_GLASS
        );

        int overriddenFriendlyBlocks = 0;

        for (int x = startX; x != startX + width * teamDirectionMultiplier; x += teamDirectionMultiplier) {
            for (int y = startY; y < startY + height; y++) {
                for (int z = startZ; z != startZ + length * teamDirectionMultiplier; z += teamDirectionMultiplier) {
                    Material block = world.getBlockAt(x, y, z).getType();

                    if (block == Material.OBSIDIAN || block == Material.NETHER_PORTAL) {
                        return false;
                    }

                    if (friendlyBlocks.contains(block)) {
                        overriddenFriendlyBlocks++;
                    }

                    boolean onFriendlySide;
                    if (team == GameTeam.GREEN) {
                        onFriendlySide = z >= 0;
                    } else {
                        onFriendlySide = z <= 0;
                    }

                    if (enemyBlocks.contains(block) && !onFriendlySide) {
                        overriddenFriendlyBlocks--;
                    }

                    if (block == Material.WHITE_STAINED_GLASS && onFriendlySide) {
                        overriddenFriendlyBlocks++;
                    }
                }
            }
        }

        return overriddenFriendlyBlocks < 5;
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
