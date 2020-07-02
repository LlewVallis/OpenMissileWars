package org.astropeci.omw.commandbuilder;

import org.astropeci.omw.commandbuilder.arguments.OptionalOverrideArgument;
import org.astropeci.omw.commandbuilder.arguments.TabOverrideArgument;

import java.util.List;
import java.util.Set;

public interface ArgumentParser<T> {
    T parse(String argument, int position, CommandContext context) throws ArgumentParseException;

    Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context);

    default boolean isOptional() {
        return false;
    }

    default ArgumentParser<T> optional() {
        return new OptionalOverrideArgument<>(this, true);
    }

    default ArgumentParser<T> setOptional(boolean optional) {
        return new OptionalOverrideArgument<>(this, optional);
    }

    default ArgumentParser<T> setCompletions(Set<String> completions) {
        return new TabOverrideArgument<>(this, (parsedArguments, currentArgument, position, context, base) -> completions);
    }

    default ArgumentParser<T> addCompletions(Set<String> completions) {
        return new TabOverrideArgument<>(this, (parsedArguments, currentArgument, position, context, base) -> {
            Set<String> result = base.complete(parsedArguments, currentArgument, position, context);
            result.addAll(completions);
            return result;
        });
    }
}
