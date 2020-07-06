package org.astropeci.omw.structures;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NBTSerializer;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import org.astropeci.omw.FileUtil;
import org.astropeci.omw.teams.GameTeam;
import org.astropeci.omw.worlds.WorldManager;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class StructureManager implements AutoCloseable {

    private final WorldManager worldManager;

    private final NBTSerializer nbtSerializer = new NBTSerializer();
    private final NBTDeserializer nbtDeserializer = new NBTDeserializer();

    private final Map<NameAndTeam, String> cachedTransformations = new HashMap<>();

    @Value
    private static class NameAndTeam {
        String name;
        GameTeam team;
    }

    public String getOrCreateTransformedStructure(String structureName, GameTeam team) throws NoSuchStructureException, IOException {
        NameAndTeam cacheKey = new NameAndTeam(structureName, team);

        if (cachedTransformations.containsKey(cacheKey)) {
            return cachedTransformations.get(cacheKey);
        }

        String newStructureName = UUID.randomUUID().toString();

        Path destFile = getStructureDirectory(Structure.TRANSFORMED_AUTHOR).resolve(newStructureName + ".nbt").normalize();
        if (Files.exists(destFile)) {
            return newStructureName;
        }

        Path sourceFile = getStructureDirectory(Structure.SOURCE_AUTHOR).resolve(structureName + ".nbt").normalize();
        if (!Files.isRegularFile(sourceFile)) {
            throw new NoSuchStructureException(structureName);
        }

        Bukkit.getLogger().info("Transforming structure " + structureName + " into " + newStructureName);

        NamedTag structureNbt = nbtDeserializer.fromStream(Files.newInputStream(sourceFile));
        transformStructureNbt(structureNbt, team);

        Files.createDirectories(destFile.getParent());

        nbtSerializer.toStream(structureNbt, Files.newOutputStream(destFile, StandardOpenOption.CREATE_NEW));

        return newStructureName;
    }

    private void transformStructureNbt(NamedTag structureNbt, GameTeam team) {
        Map<String, String> blockRewrites = Map.of(
                "minecraft:terracotta", team == GameTeam.GREEN ? "minecraft:green_terracotta" : "minecraft:red_terracotta",
                "minecraft:glass", team == GameTeam.GREEN ? "minecraft:green_stained_glass" : "minecraft:red_stained_glass"
        );

        CompoundTag root = (CompoundTag) structureNbt.getTag();

        @SuppressWarnings("unchecked")
        ListTag<CompoundTag> palette = (ListTag<CompoundTag>) root.getListTag("palette");

        // Note that -1 won't match anything, so if the structure doesn't contain air we won't remove any
        int airIndex = -1;

        for (int i = 0; i < palette.size(); i++) {
            CompoundTag paletteEntry = palette.get(i);

            String oldBlock = paletteEntry.getString("Name");
            String newBlock = blockRewrites.getOrDefault(oldBlock, oldBlock);

            paletteEntry.putString("Name", newBlock);

            if (newBlock.equals("minecraft:air")) {
                airIndex = i;
            }
        }

        @SuppressWarnings("unchecked")
        ListTag<CompoundTag> blocks = (ListTag<CompoundTag>) root.getListTag("blocks");

        for (int i = blocks.size() - 1; i >= 0; i--) {
            CompoundTag block = blocks.get(i);

            if (block.getInt("state") == airIndex) {
                blocks.remove(i);
            }
        }
    }

    public Set<String> getAllStructureNames() {
        Path structureDirectory = getStructureDirectory(Structure.SOURCE_AUTHOR);

        try {
            return Files.list(structureDirectory)
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(".nbt"))
                    .map(name -> name.substring(0, name.lastIndexOf(".nbt")))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to fetch structure list", e);
            return Set.of();
        }
    }

    private Path getStructureDirectory(String author) {
        Path worldDirectory = worldManager.getDefaultWorld().getWorldFolder().toPath();
        return worldDirectory.resolve(Path.of("generated", author, "structures"));
    }

    @Override
    public void close() {
        Path transformedDirectory = getStructureDirectory(Structure.TRANSFORMED_AUTHOR);

        try {
            FileUtil.deleteRecursive(transformedDirectory);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to clean transformed structures", e);
        }
    }
}
