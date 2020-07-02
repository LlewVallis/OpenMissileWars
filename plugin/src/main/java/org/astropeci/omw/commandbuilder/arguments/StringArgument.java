package org.astropeci.omw.commandbuilder.arguments;

import org.astropeci.omw.commandbuilder.ArgumentParser;
import org.astropeci.omw.commandbuilder.CommandContext;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class StringArgument implements ArgumentParser<String> {

    @Override
    public String parse(String argument, int position, CommandContext context) {
        return argument;
    }

    @Override
    public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
        return Collections.emptySet();
    }
}
