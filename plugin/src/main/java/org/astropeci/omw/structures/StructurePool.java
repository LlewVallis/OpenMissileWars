package org.astropeci.omw.structures;

import lombok.RequiredArgsConstructor;
import org.astropeci.omw.worlds.WorldManager;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class StructurePool {

    private final WorldManager worldManager;

    public Set<String> getAllStructureNames() {
        Path worldDirectory = worldManager.getDefaultWorld().getWorldFolder().toPath();
        Path structureDirectory = worldDirectory.resolve(Path.of("generated", Structure.AUTHOR, "structures"));

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
}
