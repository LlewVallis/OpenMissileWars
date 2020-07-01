package org.astropeci.commandbuilder.arguments;

import org.astropeci.commandbuilder.ArgumentParseException;
import org.astropeci.commandbuilder.ArgumentParser;
import org.astropeci.commandbuilder.CommandContext;

import java.util.List;
import java.util.Set;

public class TabOverrideArgument<Result, BaseArgument extends ArgumentParser<Result>> implements ArgumentParser<Result> {

    private final BaseArgument baseArgument;
    private final CompleteFunction<Result, BaseArgument> completeFunction;

    public TabOverrideArgument(BaseArgument baseArgument, CompleteFunction<Result, BaseArgument> completeFunction) {
        this.baseArgument = baseArgument;
        this.completeFunction = completeFunction;
    }

    public interface CompleteFunction<Result, BaseArgument extends ArgumentParser<Result>> {

        Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context,
                             BaseArgument baseArgument);
    }

    @Override
    public Result parse(String argument, int position, CommandContext context) throws ArgumentParseException {
        return baseArgument.parse(argument, position, context);
    }

    @Override
    public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
        return completeFunction.complete(parsedArguments, currentArgument, position, context, baseArgument);
    }

    @Override
    public boolean isOptional() {
        return baseArgument.isOptional();
    }
}
