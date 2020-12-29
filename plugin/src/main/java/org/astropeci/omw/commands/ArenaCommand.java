package org.astropeci.omw.commands;

import io.github.llewvallis.commandbuilder.*;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.worlds.ArenaPool;
import org.astropeci.omw.worlds.NamedArena;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class ArenaCommand {

    private final ArenaPool arenaPool;

    public void register(PluginCommand command) {
        new CommandBuilder()
                .argument(new ArenaArgument(arenaPool))
                .build(new ReflectionCommandCallback(this), command);
    }

    @ExecuteCommand
    @PlayerOnlyCommand
    public void execute(CommandContext ctx, NamedArena arena) {
        arena.arena.sendPlayerToLobby((Player) ctx.getSender());

        TextComponent message = new TextComponent("Sending you to " + arena.name);
        message.setColor(ChatColor.GREEN);
        ctx.getSender().spigot().sendMessage(message);
    }
}
