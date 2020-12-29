package org.astropeci.omw.commands;

import io.github.llewvallis.commandbuilder.ArgumentParseException;
import io.github.llewvallis.commandbuilder.ArgumentParser;
import io.github.llewvallis.commandbuilder.CommandContext;
import io.github.llewvallis.commandbuilder.arguments.StringSetArgument;
import org.astropeci.omw.teams.GameTeam;

import java.util.List;
import java.util.Set;

public class TeamArgument implements ArgumentParser<GameTeam> {

    private final StringSetArgument underlying = new StringSetArgument("green", "red");

    @Override
    public GameTeam parse(String argument, int position, CommandContext context) throws ArgumentParseException {
        String teamString =  underlying.parse(argument, position, context);

        switch (teamString) {
            case "green":
                return GameTeam.GREEN;
            case "red":
                return GameTeam.RED;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
        return underlying.complete(parsedArguments, currentArgument, position, context);
    }
}
