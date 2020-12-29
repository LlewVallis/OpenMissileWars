package org.astropeci.omw.commands;

import io.github.llewvallis.commandbuilder.ArgumentParseException;
import io.github.llewvallis.commandbuilder.ArgumentParser;
import io.github.llewvallis.commandbuilder.CommandContext;
import lombok.RequiredArgsConstructor;
import org.astropeci.omw.worlds.Arena;
import org.astropeci.omw.worlds.ArenaPool;
import org.astropeci.omw.worlds.NamedArena;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ArenaArgument implements ArgumentParser<NamedArena> {

    private final ArenaPool arenaPool;

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
