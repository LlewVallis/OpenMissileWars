package org.astropeci.omw.commands;

import io.github.llewvallis.commandbuilder.*;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.worlds.ArenaPool;
import org.astropeci.omw.worlds.NamedArena;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.Optional;

@RequiredArgsConstructor
public class DispenseItemCommand {

    private final ArenaPool arenaPool;

    public void register(PluginCommand command) {
        new CommandBuilder().build(new ReflectionCommandCallback(this), command);
    }

    @ExecuteCommand
    @PlayerOnlyCommand
    public void execute(CommandContext ctx) {
        Optional<NamedArena> arenaOptional = arenaPool.getPlayerArena((Player) ctx.getSender());

        if (arenaOptional.isEmpty()) {
            TextComponent message = new TextComponent("You must be in an arena to reset it");
            message.setColor(ChatColor.RED);
            ctx.getSender().sendMessage(message);
            return;
        }

        NamedArena arena = arenaOptional.get();
        arena.arena.dispenseItem();

        TextComponent message = new TextComponent("Gave everyone a random item");
        message.setColor(ChatColor.GREEN);
        ctx.getSender().sendMessage(message);
    }
}
