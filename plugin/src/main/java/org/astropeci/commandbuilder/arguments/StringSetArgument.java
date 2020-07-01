package org.astropeci.commandbuilder.arguments;

import org.astropeci.commandbuilder.ArgumentParseException;
import org.astropeci.commandbuilder.ArgumentParser;
import org.astropeci.commandbuilder.CommandContext;

import java.util.List;
import java.util.Set;

public class StringSetArgument implements ArgumentParser<String> {

    private final Set<String> possibleValues;

    public StringSetArgument(Set<String> possibleValues, boolean optional) {
        this.possibleValues = possibleValues;
    }

    @Override
    public String parse(String argument, int position, CommandContext context) throws ArgumentParseException {
        if (possibleValues.contains(argument)) {
            return argument;
        } else {
            throw new ArgumentParseException("expected one of " + possibleValues + " but found '" + argument + "'");
        }
    }

    @Override
    public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
        return possibleValues;
    }
}
