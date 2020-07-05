package org.astropeci.omw.commands;

import org.astropeci.omw.commandbuilder.ArgumentParseException;
import org.astropeci.omw.commandbuilder.ArgumentParser;
import org.astropeci.omw.commandbuilder.CommandContext;
import org.astropeci.omw.game.Arena;
import org.astropeci.omw.worlds.ArenaPool;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ArenaArgument implements ArgumentParser<NamedArena> {

    private final ArenaPool arenaPool;

    public ArenaArgument(ArenaPool arenaPool) {
        this.arenaPool = arenaPool;
    }

    @Override
    public NamedArena parse(String argument, int position, CommandContext context) throws ArgumentParseException {
        Arena arena = arenaPool.get(argument)
                .orElseThrow(() -> new ArgumentParseException("arena " + argument + " does not exist"));

        return new NamedArena(argument, arena);
    }

    @Override
    public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
        return arenaPool.getAllArenas().stream()
                .map(arena -> arena.name)
                .collect(Collectors.toSet());
    }
}
