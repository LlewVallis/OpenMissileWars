package org.astropeci.omw.worlds;

import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;

public class Template {

    private static final String TEMPLATE_WORLD_NAME = "template";
    private static final String GENERATOR_SETTINGS = "{\"layers\":[],\"biome\":\"minecraft:plains\",\"structures\":{\"stronghold\":{\"distance\":0,\"spread\":0,\"count\":0},\"structures\":{}}}";

    public static void sendPlayer(Player player) {
        Worlds.send(player, getWorld());
    }

    public static World createWorld(String name) {
        if (!name.matches("[-_a-zA-Z0-9]+")) {
            Bukkit.getLogger().warning("World name " + name + " does not appear to be well formed, this may cause issues");
        }

        World templateWorld = getWorld();
        Path source = templateWorld.getWorldFolder().toPath().resolve("region");

        Path worldContainer = Bukkit.getWorldContainer().toPath();
        Path dest = worldContainer.resolve(name).resolve("region");

        try {
            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path sourceFile, BasicFileAttributes attrs) throws IOException {
                    Path destFile = dest.resolve(source.relativize(sourceFile));
                    Files.createDirectories(destFile.getParent());
                    Files.copy(sourceFile, destFile);
                    return super.visitFile(sourceFile, attrs);
                }
            });
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to clone region files from " + source + " to " + dest, e);
        }

        WorldCreator creator = new WorldCreator(name);
        creator.copy(templateWorld);
        creator.generatorSettings(GENERATOR_SETTINGS);

        World world = creator.createWorld();
        Worlds.configureWorld(world);

        return world;
    }

    private static World getWorld() {
        World world = Bukkit.getWorld(TEMPLATE_WORLD_NAME);

        if (world == null) {
            Bukkit.getLogger().info("No template world found, generating one");

            WorldCreator creator = new WorldCreator(TEMPLATE_WORLD_NAME);
            creator.type(WorldType.FLAT);
            creator.generatorSettings(GENERATOR_SETTINGS);
            creator.generateStructures(false);
            World newWorld = creator.createWorld();
            Worlds.configureWorld(newWorld);
            return newWorld;
        } else {
            return world;
        }
    }
}
