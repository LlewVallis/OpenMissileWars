package org.astropeci.omw.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.commandbuilder.*;
import org.astropeci.omw.worlds.ArenaPool;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ArenaCommand {

    private final ArenaPool arenaPool;
    private final TabExecutor executor;

    public ArenaCommand(ArenaPool arenaPool) {
        this.arenaPool = arenaPool;

        executor = new CommandBuilder()
                .addArgument(new ArenaArgument(arenaPool))
                .build(new ReflectionCommandCallback(this));
    }

    public void register(Plugin plugin) {
        CommandBuilder.registerCommand(
                plugin,
                "arena",
                "Leave any active game and teleport to the spawn of an arena",
                "arena <arena>",
                "omw.arena.join",
                executor
        );
    }

    @PlayerOnlyCommand
    @ExecuteCommand
    public boolean execute(CommandContext ctx, NamedArena arena) {
        arena.arena.sendPlayerToLobby((Player) ctx.sender);

        TextComponent message = new TextComponent("Sending you to " + arena.name);
        message.setColor(ChatColor.GREEN);
        ctx.sender.spigot().sendMessage(message);

        return true;
    }
}
