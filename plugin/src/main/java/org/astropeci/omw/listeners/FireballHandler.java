package org.astropeci.omw.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;
import java.util.Set;

public class FireballHandler implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Fireball) {
            Fireball fireball = (Fireball) event.getEntity();

            if (fireball.getVehicle() instanceof ArmorStand) {
                ArmorStand fireballHolder = (ArmorStand) fireball.getVehicle();
                fireball.leaveVehicle();
                fireballHolder.remove();
            }
        }
    }

    //@EventHandler
    //public void onEntitySpawn(EntitySpawnEvent e) {
    //    if (e.getEntityType() == EntityType.FIREBALL) {
    //        Fireball fireball = (Fireball) e.getEntity();

    //        World world = fireball.getWorld();
    //        BoundingBox box = fireball.getBoundingBox();

    //        int minX = (int) Math.floor(box.getMinX());
    //        int minY = (int) Math.floor(box.getMinY());
    //        int minZ = (int) Math.floor(box.getMinZ());
    //        int maxX = (int) Math.ceil(box.getMaxX());
    //        int maxY = (int) Math.ceil(box.getMaxY());
    //        int maxZ = (int) Math.ceil(box.getMaxZ());
    //    }
    //}

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        Set<Material> blacklistsBlocks = Set.of(
                Material.NETHER_PORTAL,
                Material.GLASS,
                Material.WHITE_STAINED_GLASS,
                Material.ORANGE_STAINED_GLASS,
                Material.MAGENTA_STAINED_GLASS,
                Material.LIGHT_BLUE_STAINED_GLASS,
                Material.YELLOW_STAINED_GLASS,
                Material.LIME_STAINED_GLASS,
                Material.PINK_STAINED_GLASS,
                Material.GRAY_STAINED_GLASS,
                Material.LIGHT_GRAY_STAINED_GLASS,
                Material.CYAN_STAINED_GLASS,
                Material.PURPLE_STAINED_GLASS,
                Material.BLUE_STAINED_GLASS,
                Material.BROWN_STAINED_GLASS,
                Material.GREEN_STAINED_GLASS,
                Material.RED_STAINED_GLASS,
                Material.BLACK_STAINED_GLASS
        );

        if (e.getEntityType() == EntityType.FIREBALL) {
            List<Block> blockList = e.blockList();
            blockList.removeIf(block -> blacklistsBlocks.contains(block.getType()));
        }
    }
}
