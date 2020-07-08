package org.astropeci.omw.structures;

import lombok.Getter;
import org.astropeci.omw.teams.GameTeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.logging.Level;

import static org.astropeci.omw.ReflectionUtil.fetchClass;

public class Structure {

    @Getter
    private final String name;

    private final StructureManager structureManager;

    /* package-private */ static final String SOURCE_AUTHOR = "openmissilewars";
    /* package-private */ static final String TRANSFORMED_AUTHOR = "openmissilewars-transformed";

    private static String serverVersionCache = null;

    public Structure(String name, StructureManager structureManager) throws NoSuchStructureException {
        if (!structureManager.getAllStructureNames().contains(name)) {
            throw new NoSuchStructureException(name, name + " does not exist");
        }

        this.name = name;
        this.structureManager = structureManager;
    }

    public enum Rotation {
        ROTATE_0,
        ROTATE_90,
        ROTATE_180,
        ROTATE_270,
    }

    public boolean load(Location location, GameTeam team, Rotation rotation) {
        String structureName;
        try {
            structureName = structureManager.getOrCreateTransformedStructure(name, team);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Exception transforming structure, check your Paper/OMW version", e);
            return false;
        }

        try {
            loadUnsafe(location, structureName, rotation);
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Exception cloning structure, check your Paper/OMW version", e);
            return false;
        }
    }

    private void loadUnsafe(Location target, String structureName, Rotation rotation) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException, InstantiationException {
        Bukkit.getLogger().fine("Attempting to spawn structure " + structureName + " at " + target);

        if (isClientSide(target.getWorld())) {
            return;
        }

        delegateLoad(target, structureName, rotation);
    }

    /*
     * Here comes NMS reflection blood magic...
     */

    private void delegateLoad(Location target, String structureName, Rotation rotation) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Class<?> c_definedStructureInfo = fetchClass("$NMS.DefinedStructureInfo");
        Class<?> c_blockPosition = fetchClass("$NMS.BlockPosition");
        Class<?> c_generatorAccess = fetchClass("$NMS.GeneratorAccess");
        Class<?> c_definedStructure = fetchClass("$NMS.DefinedStructure");

        Object targetBlockPosition = getTargetBlockPosition(target);
        Object definedStructure = getDefinedStructure(structureName);
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

    private Object getDefinedStructure(String structureName) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> c_minecraftKey = fetchClass("$NMS.MinecraftKey");
        Class<?> c_worldServer = fetchClass("$NMS.WorldServer");
        Class<?> c_definedStructureManager = fetchClass("$NMS.DefinedStructureManager");

        Object nmsSourceWorlds = getNmsWorld(Bukkit.getWorld("world"));
        Object structureManager = c_worldServer.getDeclaredMethod("r_").invoke(nmsSourceWorlds);

        Object minecraftKey = c_minecraftKey.getDeclaredConstructor(String.class, String.class).newInstance(TRANSFORMED_AUTHOR, structureName);

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
}
