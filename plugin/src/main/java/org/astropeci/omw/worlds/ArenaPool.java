package org.astropeci.omw.worlds;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ArenaPool implements AutoCloseable {

    private final Template template;

    private final Map<String, Arena> arenas = new HashMap<>();

    public Arena create(String name) throws ArenaAlreadyExistsException {
        if (arenas.containsKey(name)) {
            Bukkit.getLogger().info("Failed to create arena " + name + " as it already exists");
            throw new ArenaAlreadyExistsException();
        }

        Bukkit.getLogger().info("Creating arena " + name);
        Arena arena = template.createArena();
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

    public Optional<NamedArena> getPlayerArena(Player player) {
        return getArenaForWorld(player.getWorld());
    }

    public Optional<NamedArena> getArenaForWorld(World world) {
        for (NamedArena arena : getAllArenas()) {
            if (arena.arena.isAttachedToWorld(world)) {
                return Optional.of(arena);
            }
        }

        return Optional.empty();
    }

    public Optional<Arena> get(String name) {
        return Optional.ofNullable(arenas.get(name));
    }

    public Set<NamedArena> getAllArenas() {
        return arenas.entrySet().stream()
                .map(entry -> new NamedArena(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }

    @SneakyThrows({ ArenaAlreadyExistsException.class })
    public void resetArena(String name) throws NoSuchArenaException {
        String tempName = "temp-" + UUID.randomUUID().toString();

        get(name).orElseThrow(() -> new NoSuchArenaException(name, name + " does not exist"));

        Arena newArena = create(tempName);
        delete(name);

        arenas.remove(tempName);
        arenas.put(name, newArena);
    }

    @Override
    @SneakyThrows({ NoSuchArenaException.class })
    public void close() {
        // Avoid modification during iteration
        Set<String> arenaNames = new HashSet<>(arenas.keySet());

        for (String name : arenaNames) {
            delete(name);
        }
    }
}
