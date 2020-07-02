package org.astropeci.omw.commandbuilder.arguments;

import org.astropeci.omw.commandbuilder.ArgumentParseException;
import org.astropeci.omw.commandbuilder.ArgumentParser;
import org.astropeci.omw.commandbuilder.CommandContext;

import java.util.List;
import java.util.Set;

public class OptionalOverrideArgument<Result, BaseArgument extends ArgumentParser<Result>> implements ArgumentParser<Result> {

    private final BaseArgument baseArgument;
    private final boolean optional;

    public OptionalOverrideArgument(BaseArgument baseArgument, boolean optional) {
        this.baseArgument = baseArgument;
        this.optional = optional;
    }

    @Override
    public Result parse(String argument, int position, CommandContext context) throws ArgumentParseException {
        return baseArgument.parse(argument, position, context);
    }

    @Override
    public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
        return baseArgument.complete(parsedArguments, currentArgument, position, context);
    }

    @Override
    public boolean isOptional() {
        return optional;
    }
}
