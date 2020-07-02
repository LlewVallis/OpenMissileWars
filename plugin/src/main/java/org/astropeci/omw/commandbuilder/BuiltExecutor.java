package org.astropeci.omw.commandbuilder;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.*;
import java.util.stream.Collectors;

/* package-private */ class BuiltExecutor implements TabExecutor {

    private final List<ArgumentParser<?>> parsers;
    private final ArgumentParser<?> variadicParser;
    private final CommandCallback callback;

    /* package-private */ BuiltExecutor(List<ArgumentParser<?>> parsers, ArgumentParser<?> variadicParser, CommandCallback callback) {
        this.parsers = parsers;
        this.variadicParser = variadicParser;
        this.callback = callback;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] argumentStrings) {
        CommandContext context = new CommandContext(sender, command, alias, List.of(argumentStrings));

        try {
            List<Object> argumentValues = new ArrayList<>();
            List<Object> variadicArgumentValues = new ArrayList<>();

            tryParseCommand(argumentStrings, context, argumentValues, variadicArgumentValues);

            // Pass null as opposed to an empty list in the case that the command is not variadic
            if (!isVariadic()) {
                variadicArgumentValues = null;
            }

            return callback.onSuccess(argumentValues, variadicArgumentValues, context);
        } catch (CommandParseException e) {
            return callback.onFailure(e, context);
        }
    }

    private void tryParseCommand(String[] argumentStrings, CommandContext context, List<Object> argumentValues, List<Object> variadicArgumentValues) throws CommandParseException {
        int parserIndex = 0;
        int argumentStringIndex = 0;

        for (; parserIndex < parsers.size(); parserIndex++, argumentStringIndex++) {
            if (argumentStringIndex >= argumentStrings.length) {
                for (int i = parserIndex; i < parsers.size(); i++) {
                    if (!parsers.get(i).isOptional()) {
                        throw new CommandParseException("not enough arguments");
                    }

                    argumentValues.add(null);
                }

                break;
            }

            ArgumentParser parser = parsers.get(parserIndex);
            String argumentString = argumentStrings[argumentStringIndex];

            try {
                Object value = parser.parse(argumentString, argumentStringIndex, context);
                argumentValues.add(value);
            } catch (ArgumentParseException e) {
                if (parser.isOptional()) {
                    argumentValues.add(null);
                    argumentStringIndex--;
                } else {
                    throw new CommandParseException("invalid argument: " + argumentString + ", " + e.getMessage());
                }
            }
        }

        if (argumentStringIndex <= argumentStrings.length - 1) {
            if (isVariadic()) {
                for (; argumentStringIndex < argumentStrings.length; argumentStringIndex++) {
                    String argumentString = argumentStrings[argumentStringIndex];

                    try {
                        Object value = variadicParser.parse(argumentString, argumentStringIndex, context);
                        variadicArgumentValues.add(value);
                    } catch (ArgumentParseException e) {
                        throw new CommandParseException("invalid argument: " + argumentString + ", " + e.getMessage());
                    }
                }
            } else {
                throw new CommandParseException("too many arguments");
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] argumentStrings) {
        // Shouldn't happen, but handle it nicely just in-case
        if (argumentStrings.length == 0) {
            Bukkit.getLogger().warning("received zero length argument list when tab completing '" + alias + "'");
            return Collections.emptyList();
        }

        CommandContext context = new CommandContext(sender, command, alias, List.of(argumentStrings));

        String partialArgument = argumentStrings[argumentStrings.length - 1];

        Set<String> availableCompletes = availableCompletes(context, argumentStrings);
        List<String> trimmedCompletes = availableCompletes.stream()
                .filter(complete -> complete.toLowerCase().startsWith(partialArgument.toLowerCase()))
                .collect(Collectors.toList());

        return trimmedCompletes;
    }

    private Set<String> availableCompletes(CommandContext context, String[] argumentStrings) {
        List<Object> parsedValues = new ArrayList<>();

        int argumentStringIndex = 0;

        for (ArgumentParser<?> parser : parsers) {
            String argument = argumentStrings[argumentStringIndex];

            if (argumentStringIndex == argumentStrings.length - 1) {
                // TODO Pull completes form multiple parsers if the current one is optional
                return parser.complete(parsedValues, argument, argumentStringIndex, context);
            }

            try {
                Object parsedValue = parser.parse(argument, argumentStringIndex, context);
                parsedValues.add(parsedValue);
                argumentStringIndex++;
            } catch (ArgumentParseException e) {
                parsedValues.add(null);

                if (!parser.isOptional()) {
                    argumentStringIndex++;
                }
            }
        }

        if (this.isVariadic()) {
            for (; argumentStringIndex < argumentStrings.length - 1; argumentStringIndex++) {
                String argument = argumentStrings[argumentStringIndex];

                try {
                    Object parsedValue = variadicParser.parse(argument, argumentStringIndex, context);
                    parsedValues.add(parsedValue);
                } catch (ArgumentParseException e) {
                    parsedValues.add(null);
                }
            }

            String argument = argumentStrings[argumentStringIndex];
            return variadicParser.complete(parsedValues, argument, argumentStringIndex, context);
        } else {
            return Collections.emptySet();
        }
    }

    public boolean isVariadic() {
        return variadicParser != null;
    }
}
