package org.astropeci.omw.commands;

import io.github.llewvallis.commandbuilder.ArgumentParseException;
import io.github.llewvallis.commandbuilder.ArgumentParser;
import io.github.llewvallis.commandbuilder.CommandContext;
import lombok.RequiredArgsConstructor;
import org.astropeci.omw.structures.NoSuchStructureException;
import org.astropeci.omw.structures.Structure;
import org.astropeci.omw.structures.StructureManager;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class StructureArgument implements ArgumentParser<Structure> {

    private final StructureManager structureManager;

    @Override
    public Structure parse(String argument, int position, CommandContext context) throws ArgumentParseException {
        try {
            return new Structure(argument, structureManager);
        } catch (NoSuchStructureException e) {
            throw new ArgumentParseException("structure " + argument + " does not exist");
        }
    }

    @Override
    public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
        return structureManager.getAllStructureNames();
    }
}
