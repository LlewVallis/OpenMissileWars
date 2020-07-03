package org.astropeci.omw.commands;

import org.astropeci.omw.commandbuilder.ArgumentParseException;
import org.astropeci.omw.commandbuilder.ArgumentParser;
import org.astropeci.omw.commandbuilder.CommandContext;
import org.astropeci.omw.structures.NoSuchStructureException;
import org.astropeci.omw.structures.Structure;

import java.util.List;
import java.util.Set;

public class StructureArgument implements ArgumentParser<Structure> {

    @Override
    public Structure parse(String argument, int position, CommandContext context) throws ArgumentParseException {
        try {
            return new Structure(argument);
        } catch (NoSuchStructureException e) {
            throw new ArgumentParseException("structure " + argument + " does not exist");
        }
    }

    @Override
    public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
        return Structure.allStructures();
    }
}
