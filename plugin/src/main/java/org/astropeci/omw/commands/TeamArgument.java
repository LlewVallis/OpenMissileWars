package org.astropeci.omw.commands;

import org.astropeci.omw.commandbuilder.ArgumentParseException;
import org.astropeci.omw.commandbuilder.ArgumentParser;
import org.astropeci.omw.commandbuilder.CommandContext;
import org.astropeci.omw.commandbuilder.arguments.StringSetArgument;
import org.astropeci.omw.game.GameTeam;

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
