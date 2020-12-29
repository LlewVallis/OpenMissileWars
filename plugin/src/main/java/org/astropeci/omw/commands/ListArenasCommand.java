package org.astropeci.omw.commands;

import io.github.llewvallis.commandbuilder.CommandBuilder;
import io.github.llewvallis.commandbuilder.CommandContext;
import io.github.llewvallis.commandbuilder.ExecuteCommand;
import io.github.llewvallis.commandbuilder.ReflectionCommandCallback;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.worlds.ArenaPool;
import org.bukkit.command.PluginCommand;

import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ListArenasCommand {

    private final ArenaPool arenaPool;

    public void register(PluginCommand command) {
        new CommandBuilder().build(new ReflectionCommandCallback(this), command);
    }

    @ExecuteCommand
    public void execute(CommandContext ctx) {
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

        ctx.getSender().spigot().sendMessage(message);
    }
}
