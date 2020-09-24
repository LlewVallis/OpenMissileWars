package org.astropeci.omw.structures;

import com.github.shynixn.structureblocklib.api.bukkit.StructureBlockLibApi;
import com.github.shynixn.structureblocklib.api.enumeration.StructureRotation;
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

        StructureRotation apiRotation = null;
        switch (rotation) {
            case ROTATE_0:
                apiRotation = StructureRotation.NONE;
                break;
            case ROTATE_90:
                apiRotation = StructureRotation.ROTATION_90;
                break;
            case ROTATE_180:
                apiRotation = StructureRotation.ROTATION_180;
                break;
            case ROTATE_270:
                apiRotation = StructureRotation.ROTATION_270;
                break;
        }

        StructureBlockLibApi.INSTANCE
                .loadStructure(structureManager.getPlugin())
                .at(target)
                .rotation(apiRotation)
                .loadFromPath(structureManager.getStructureDirectory(TRANSFORMED_AUTHOR).resolve(structureName + ".nbt"))
                .onException(e -> Bukkit.getLogger().log(Level.WARNING, "Failed to load structure", e));
    }
}
