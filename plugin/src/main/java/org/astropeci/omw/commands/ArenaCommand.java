package org.astropeci.omw.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.commandbuilder.*;
import org.astropeci.omw.commandbuilder.arguments.StringArgument;
import org.astropeci.omw.commandbuilder.arguments.StringSetArgument;
import org.astropeci.omw.worlds.Arena;
import org.astropeci.omw.worlds.ArenaPool;
import org.astropeci.omw.worlds.Template;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

public class ArenaCommand {

    private final ArenaPool arenaPool;
    private final TabExecutor executor;

    public ArenaCommand(ArenaPool arenaPool) {
        this.arenaPool = arenaPool;

        executor = new CommandBuilder()
                .addArgument(new StringArgument())
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
    public boolean execute(CommandContext ctx, String arenaName) {
        Optional<Arena> arenaOptional = arenaPool.get(arenaName);

        arenaOptional.ifPresentOrElse(arena -> {
            arena.sendPlayer((Player) ctx.sender);

            TextComponent message = new TextComponent("Sending you to " + arenaName);
            message.setColor(ChatColor.GREEN);
            ctx.sender.spigot().sendMessage(message);
        }, () -> {
            TextComponent message = new TextComponent("No arena exists called " + arenaName);
            message.setColor(ChatColor.RED);
            ctx.sender.spigot().sendMessage(message);

        });

        return true;
    }
}
