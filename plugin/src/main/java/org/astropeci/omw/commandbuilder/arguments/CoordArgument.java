package org.astropeci.omw.commandbuilder.arguments;

import org.astropeci.omw.commandbuilder.ArgumentParseException;
import org.astropeci.omw.commandbuilder.ArgumentParser;
import org.astropeci.omw.commandbuilder.CommandContext;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Entity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// TODO Add support for caret coordinates
public class CoordArgument implements ArgumentParser<Integer> {

    private final Axis axis;

    public CoordArgument(Axis axis) {
        this.axis = axis;
    }

    public enum Axis {
        X, Y, Z
    }

    @Override
    public Integer parse(String argument, int position, CommandContext context) throws ArgumentParseException {
        boolean relative = argument.startsWith("~");
        if (relative) {
            argument = argument.substring(1);
        }

        int value;
        if (relative && argument.equals("")) {
            value = 0;
        } else {
            try {

                value = Integer.parseInt(argument);
            } catch (NumberFormatException e) {
                throw new ArgumentParseException("invalid coordinate");
            }
        }

        if (relative) {
            value += getSenderCoord(context.sender).orElseThrow(() ->
                    new ArgumentParseException("cannot use relative coordinates int this context"));
        }

        return value;
    }

    @Override
    public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
        return getSenderCoord(context.sender)
                .map(Object::toString)
                .map(Set::of)
                .orElse(Collections.emptySet());
    }

    private Optional<Integer> getSenderCoord(CommandSender sender) {
        if (sender instanceof Entity)  {
            Location location = ((Entity) sender).getLocation();

            switch (axis) {
                case X:
                    return Optional.of(location.getBlockX());
                case Y:
                    return Optional.of(location.getBlockY());
                case Z:
                    return Optional.of(location.getBlockZ());
            }
        }

        if (sender instanceof BlockCommandSender) {
            Block block = ((BlockCommandSender) sender).getBlock();

            switch (axis) {
                case X:
                    return Optional.of(block.getX());
                case Y:
                    return Optional.of(block.getY());
                case Z:
                    return Optional.of(block.getZ());
            }
        }

        if (sender instanceof ProxiedCommandSender) {
            return getSenderCoord(((ProxiedCommandSender) sender).getCallee());
        }

        return Optional.empty();
    }
}
