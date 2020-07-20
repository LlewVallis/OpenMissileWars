package org.astropeci.omw.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.astropeci.omw.ReflectionUtil.fetchClass;

public class ExplosionModifier implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        List<Block> blocks = e.blockList();

        for (Block block : blocks) {
            if (block.getType() == Material.MOVING_PISTON && doesMovingPistonContainBlock(block, "BlockTNT")) {
                createTnt(block.getLocation());
            }
        }
    }

    @EventHandler
    public void onFireballExplode(EntityExplodeEvent e) {
        if (e.getEntityType() == EntityType.FIREBALL) {
            Set<Material> blacklistedBlocks = Set.of(
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

            List<Block> blockList = e.blockList();
            blockList.removeIf(block -> blacklistedBlocks.contains(block.getType()));
            blockList.removeIf(block -> block.getType() == Material.MOVING_PISTON &&
                    doesMovingPistonContainBlock(block, "BlockGlassAbstract"));
        }
    }

    private boolean doesMovingPistonContainBlock(Block block, String blockClassName) {
        World world = block.getWorld();

        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        try {
            Class<?> c_craftWorld = fetchClass("$OBC.CraftWorld");
            Class<?> c_worldServer = fetchClass("$NMS.WorldServer");
            Class<?> c_blockPosition = fetchClass("$NMS.BlockPosition");
            Class<?> c_tileEntityPiston = fetchClass("$NMS.TileEntityPiston");
            Class<?> c_iBlockDataHolder = fetchClass("$NMS.IBlockDataHolder");
            Class<?> blockOfInterest = fetchClass("$NMS." + blockClassName);

            Method m_craftWorld_getHandle = c_craftWorld.getDeclaredMethod("getHandle");
            Object nmsWorld = m_craftWorld_getHandle.invoke(world);

            Object blockPosition = c_blockPosition.getConstructor(int.class, int.class, int.class).newInstance(x, y, z);

            Method m_worldServer_getTileEntity = c_worldServer.getDeclaredMethod("getTileEntity", c_blockPosition, boolean.class);
            m_worldServer_getTileEntity.setAccessible(true);
            Object tileEntity = m_worldServer_getTileEntity.invoke(nmsWorld, blockPosition, true);

            Field f_tileEntityPiston_a = c_tileEntityPiston.getDeclaredField("a");
            f_tileEntityPiston_a.setAccessible(true);
            Object blockData = f_tileEntityPiston_a.get(tileEntity);

            Field f_iBlockDataHolder_c = c_iBlockDataHolder.getDeclaredField("c");
            f_iBlockDataHolder_c.setAccessible(true);
            Object movingBlock = f_iBlockDataHolder_c.get(blockData);

            return blockOfInterest.isInstance(movingBlock);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to handle exploding piston extension", e);
        }

        return false;
    }

    private void createTnt(Location location) {
        World world = location.getWorld();
        location.add(0.5, 0, 0.5);

        TNTPrimed tnt = (TNTPrimed) world.spawnEntity(location, EntityType.PRIMED_TNT);

        int fuse = random.nextInt(20) + 10;
        tnt.setFuseTicks(fuse);
    }

}
