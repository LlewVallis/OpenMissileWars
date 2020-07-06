package org.astropeci.omw.commands;

import lombok.SneakyThrows;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.commandbuilder.CommandBuilder;
import org.astropeci.omw.commandbuilder.CommandContext;
import org.astropeci.omw.commandbuilder.ExecuteCommand;
import org.astropeci.omw.commandbuilder.ReflectionCommandCallback;
import org.astropeci.omw.commandbuilder.arguments.StringArgument;
import org.astropeci.omw.worlds.ArenaAlreadyExistsException;
import org.astropeci.omw.worlds.ArenaPool;
import org.astropeci.omw.worlds.NoSuchArenaException;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;

public class DeleteArenaCommand {

    private ArenaPool arenaPool;
    private TabExecutor executor;

    public DeleteArenaCommand(ArenaPool arenaPool) {
        this.arenaPool = arenaPool;

        executor = new CommandBuilder()
                .addArgument(new ArenaArgument(arenaPool))
                .build(new ReflectionCommandCallback(this));
    }

    public void register(Plugin plugin) {
        CommandBuilder.registerCommand(
                plugin,
                "arena-delete",
                "Deletes an arena",
                "arena-delete <name>",
                "omw.arena.delete",
                executor
        );
    }

    @ExecuteCommand
    @SneakyThrows({ NoSuchArenaException.class })
    public boolean execute(CommandContext ctx, NamedArena arena) {
        arenaPool.delete(arena.name);

        TextComponent message = new TextComponent("Deleted " + arena.name);
        message.setColor(ChatColor.GREEN);
        ctx.sender.spigot().sendMessage(message);

        return true;
    }
}
