package org.astropeci.commandbuilder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandContext {

    public final CommandSender sender;
    public final Command command;
    public final String alias;
    public final List<String> argumentStrings;

    public CommandContext(CommandSender sender, Command command, String alias, List<String> argumentStrings) {
        this.sender = sender;
        this.command = command;
        this.alias = alias;
        this.argumentStrings = argumentStrings;
    }
}
