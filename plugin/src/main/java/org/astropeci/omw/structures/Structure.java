package org.astropeci.omw.structures;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.logging.Level;

public class Structure {

    private final String name;

    /* package-private */ static final String AUTHOR = "openmissilewars";
    private static String serverVersionCache = null;

    public Structure(String name, StructurePool structurePool) throws NoSuchStructureException {
        if (!structurePool.getAllStructureNames().contains(name)) {
            throw new NoSuchStructureException(name, name + " does not exist");
        }

        this.name = name;
    }

    public enum Rotation {
        ROTATE_0,
        ROTATE_90,
        ROTATE_180,
        ROTATE_270,
    }

    public boolean load(Location location, Rotation rotation) {
        try {
            loadThrowing(location, rotation);
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Exception cloning structure, check your Paper/OMW version", e);
            return false;
        }
    }

    private void loadThrowing(Location target, Rotation rotation) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException, InstantiationException {
        Bukkit.getLogger().fine("Attempting to spawn structure " + name + " at " + target);

        if (isClientSide(target.getWorld())) {
            return;
        }

        delegateLoad(target, rotation);
    }

    private void delegateLoad(Location target, Rotation rotation) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Class<?> c_definedStructureInfo = fetchClass("$NMS.DefinedStructureInfo");
        Class<?> c_blockPosition = fetchClass("$NMS.BlockPosition");
        Class<?> c_generatorAccess = fetchClass("$NMS.GeneratorAccess");
        Class<?> c_definedStructure = fetchClass("$NMS.DefinedStructure");

        Object targetBlockPosition = getTargetBlockPosition(target);
        Object definedStructure = getDefinedStructure();
        Object definedStructureInfo = getDefinedStructureInfo(rotation);
        Object targetWorld = getNmsWorld(target.getWorld());

        Method m_definedStructure_a = c_definedStructure.getDeclaredMethod("a", c_generatorAccess, c_blockPosition, c_definedStructureInfo, Random.class);
        m_definedStructure_a.invoke(definedStructure, targetWorld, targetBlockPosition, definedStructureInfo, new Random());
    }

    private boolean isClientSide(World world) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        Class<?> c_world = fetchClass("$NMS.World");

        Object nmsWorld = getNmsWorld(world);
        return c_world.getDeclaredField("isClientSide").getBoolean(nmsWorld);
    }

    private Object getNmsWorld(World world) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> c_craftWorld = fetchClass("$OBC.CraftWorld");
        return c_craftWorld.getDeclaredMethod("getHandle").invoke(world);
    }

    private Object getDefinedStructure() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> c_minecraftKey = fetchClass("$NMS.MinecraftKey");
        Class<?> c_worldServer = fetchClass("$NMS.WorldServer");
        Class<?> c_definedStructureManager = fetchClass("$NMS.DefinedStructureManager");

        Object nmsSourceWorlds = getNmsWorld(Bukkit.getWorld("world"));
        Object structureManager = c_worldServer.getDeclaredMethod("r_").invoke(nmsSourceWorlds);

        Object minecraftKey = c_minecraftKey.getDeclaredConstructor(String.class, String.class).newInstance(AUTHOR, name);

        Method m_definedStructureManager_a = c_definedStructureManager.getDeclaredMethod("a", c_minecraftKey);
        return m_definedStructureManager_a.invoke(structureManager, minecraftKey);
    }

    private Object getTargetBlockPosition(Location target) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> c_blockPosition = fetchClass("$NMS.BlockPosition");
        Class<?> c_baseBlockPosition = fetchClass("$NMS.BaseBlockPosition");

        Constructor<?> i_blockPosition = c_blockPosition.getConstructor(int.class, int.class, int.class);
        Object originBlockPosition = i_blockPosition.newInstance(0, 0, 0);
        Object targetOffsetBlockPosition = i_blockPosition.newInstance(target.getBlockX(), target.getBlockY(), target.getBlockZ());

        Method m_blockPosition_a = c_blockPosition.getDeclaredMethod("a", c_baseBlockPosition);
        return m_blockPosition_a.invoke(originBlockPosition, targetOffsetBlockPosition);
    }

    private Object getDefinedStructureInfo(Rotation rotation) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Class<?> c_definedStructureInfo = fetchClass("$NMS.DefinedStructureInfo");
        Class<?> c_enumBlockMirror = fetchClass("$NMS.EnumBlockMirror");
        Class<?> c_enumBlockRotation = fetchClass("$NMS.EnumBlockRotation");
        Class<?> c_chunkCoordIntPair = fetchClass("$NMS.ChunkCoordIntPair");

        Object blockMirror = getBlockMirror();
        Object blockRotation = getBlockRotation(rotation);

        Object definedStructureInfo = c_definedStructureInfo.getDeclaredConstructor().newInstance();
        c_definedStructureInfo.getDeclaredMethod("a", c_enumBlockMirror).invoke(definedStructureInfo, blockMirror);
        c_definedStructureInfo.getDeclaredMethod("a", c_enumBlockRotation).invoke(definedStructureInfo, blockRotation);
        c_definedStructureInfo.getDeclaredMethod("a", boolean.class).invoke(definedStructureInfo, true);
        c_definedStructureInfo.getDeclaredMethod("a", c_chunkCoordIntPair).invoke(definedStructureInfo, new Object[] { null });
        c_definedStructureInfo.getDeclaredMethod("c", boolean.class).invoke(definedStructureInfo, false);

        return definedStructureInfo;
    }

    private Object getBlockMirror() throws ClassNotFoundException {
        Class<?> c_enumBlockMirror = fetchClass("$NMS.EnumBlockMirror");

        @SuppressWarnings("unchecked")
        Object blockMirror = Enum.valueOf((Class) c_enumBlockMirror, "NONE");
        return blockMirror;
    }

    private Object getBlockRotation(Rotation rotation) throws ClassNotFoundException {
        Class<?> c_enumBlockRotation = fetchClass("$NMS.EnumBlockRotation");

        String rotationValueName = null;
        switch (rotation) {
            case ROTATE_0:
                rotationValueName = "NONE";
                break;
            case ROTATE_90:
                rotationValueName = "CLOCKWISE_90";
                break;
            case ROTATE_180:
                rotationValueName = "CLOCKWISE_180";
                break;
            case ROTATE_270:
                rotationValueName = "COUNTERCLOCKWISE_90";
                break;
        }

        @SuppressWarnings("unchecked")
        Object blockRotation = Enum.valueOf((Class) c_enumBlockRotation, rotationValueName);
        return blockRotation;
    }

    private Class<?> fetchClass(String nameTemplate) throws ClassNotFoundException {
        String name = nameTemplate.replace("$OBC", "org.bukkit.craftbukkit.$VER")
                .replace("$NMS", "net.minecraft.server.$VER")
                .replace("$VER", getServerVersion());

        return Class.forName(name);
    }

    private String getServerVersion() {
        if (serverVersionCache == null) {
            serverVersionCache = Bukkit.getServer().getClass().getPackageName().split("\\.")[3];
            Bukkit.getLogger().info("Detected server version as " + serverVersionCache);
        }

        return serverVersionCache;
    }

    public String getName() {
        return name;
    }
}
