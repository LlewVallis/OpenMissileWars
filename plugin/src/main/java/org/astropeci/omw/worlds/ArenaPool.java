package org.astropeci.omw.worlds;

import org.bukkit.Bukkit;

import java.util.*;

public class ArenaPool implements AutoCloseable {

    private Map<String, Arena> arenas = new HashMap<>();

    public Arena create(String name) throws ArenaAlreadyExistsException {
        if (arenas.containsKey(name)) {
            Bukkit.getLogger().info("Failed to create arena " + name + " as it already exists");
            throw new ArenaAlreadyExistsException();
        }

        Bukkit.getLogger().info("Creating arena " + name);
        Arena arena = Arena.create();
        arenas.put(name, arena);

        return arena;
    }

    public void delete(String name) throws NoSuchArenaException {
        Arena arena = arenas.remove(name);

        if (arena == null) {
            throw new NoSuchArenaException();
        } else {
            Bukkit.getLogger().info("Deleting arena " + name);
            arena.close();
        }
    }

    public Optional<Arena> get(String name) {
        return Optional.ofNullable(arenas.get(name));
    }

    public Set<Map.Entry<String, Arena>> allArenas() {
        return arenas.entrySet();
    }

    @Override
    public void close() {
        // Avoid modification during iteration
        Set<String> arenaNames = new HashSet<>(arenas.keySet());

        for (String name : arenaNames) {
            try {
                delete(name);
            } catch (NoSuchArenaException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
