package org.astropeci.omw.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.commandbuilder.CommandBuilder;
import org.astropeci.omw.commandbuilder.CommandContext;
import org.astropeci.omw.commandbuilder.ExecuteCommand;
import org.astropeci.omw.commandbuilder.ReflectionCommandCallback;
import org.astropeci.omw.worlds.ArenaPool;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;

import java.util.stream.Collectors;

public class ListArenasCommand {

    private final ArenaPool arenaPool;
    private final TabExecutor executor;

    public ListArenasCommand(ArenaPool arenaPool) {
        this.arenaPool = arenaPool;

        executor = new CommandBuilder().build(new ReflectionCommandCallback(this));
    }

    public void register(Plugin plugin) {
        CommandBuilder.registerCommand(
                plugin,
                "arenas",
                "Lists all arenas",
                "arenas",
                "omw.arena.list",
                executor
        );
    }

    @ExecuteCommand
    public boolean execute(CommandContext ctx) {
        String arenaString = arenaPool.getAllArenas().stream()
                .map(arena -> arena.name)
                .collect(Collectors.joining(", "));

        TextComponent message;

        if (arenaString.equals("")) {
            message = new TextComponent("No arenas found");
            message.setColor(ChatColor.RED);
        } else {
            TextComponent prefix = new TextComponent("Arenas: ");
            prefix.setColor(ChatColor.GREEN);

            TextComponent suffix = new TextComponent(arenaString);
            message = new TextComponent(prefix, suffix);
        }

        ctx.sender.spigot().sendMessage(message);

        return true;
    }
}
