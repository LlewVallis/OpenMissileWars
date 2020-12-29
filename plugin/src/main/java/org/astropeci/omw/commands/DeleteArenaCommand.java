package org.astropeci.omw.commands;

import io.github.llewvallis.commandbuilder.CommandBuilder;
import io.github.llewvallis.commandbuilder.CommandContext;
import io.github.llewvallis.commandbuilder.ExecuteCommand;
import io.github.llewvallis.commandbuilder.ReflectionCommandCallback;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.worlds.ArenaPool;
import org.astropeci.omw.worlds.NamedArena;
import org.astropeci.omw.worlds.NoSuchArenaException;
import org.bukkit.command.PluginCommand;

@RequiredArgsConstructor
public class DeleteArenaCommand {

    private final ArenaPool arenaPool;

    public void register(PluginCommand command) {
        new CommandBuilder()
                .argument(new ArenaArgument(arenaPool))
                .build(new ReflectionCommandCallback(this), command);
    }

    @ExecuteCommand
    @SneakyThrows({ NoSuchArenaException.class })
    public void execute(CommandContext ctx, NamedArena arena) {
        arenaPool.delete(arena.name);

        TextComponent message = new TextComponent("Deleted " + arena.name);
        message.setColor(ChatColor.GREEN);
        ctx.getSender().spigot().sendMessage(message);
    }
}
