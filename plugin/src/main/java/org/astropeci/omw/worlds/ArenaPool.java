package org.astropeci.omw.worlds;

import org.astropeci.omw.commands.NamedArena;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.stream.Collectors;

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
            throw new NoSuchArenaException(name, name + " does not exist");
        } else {
            Bukkit.getLogger().info("Deleting arena " + name);
            arena.close();
        }
    }

    public Optional<Arena> get(String name) {
        return Optional.ofNullable(arenas.get(name));
    }

    public Set<NamedArena> allArenas() {
        return arenas.entrySet().stream()
                .map(entry -> new NamedArena(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
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
