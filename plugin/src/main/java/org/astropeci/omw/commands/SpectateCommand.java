package org.astropeci.omw.commands;

import io.github.llewvallis.commandbuilder.*;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.worlds.ArenaPool;
import org.astropeci.omw.worlds.NamedArena;
import org.bukkit.GameMode;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.Optional;

@RequiredArgsConstructor
public class SpectateCommand {

    private final ArenaPool arenaPool;

    public void register(PluginCommand command) {
        new CommandBuilder().build(new ReflectionCommandCallback(this), command);
    }

    @ExecuteCommand
    @PlayerOnlyCommand
    public void execute(CommandContext ctx) {
        Player player = (Player) ctx.getSender();

        Optional<NamedArena> arenaOptional = arenaPool.getPlayerArena(player);

        if (arenaOptional.isEmpty()) {
            TextComponent message = new TextComponent("You must be in an arena to spectate");
            message.setColor(ChatColor.RED);
            ctx.getSender().sendMessage(message);
            return;
        }

        GameMode previousGamemode = player.getGameMode();

        NamedArena arena = arenaOptional.get();
        arena.arena.sendPlayerToLobby(player);

        TextComponent message;

        if (previousGamemode == GameMode.SPECTATOR) {
            message = new TextComponent("You are no longer spectating");
        } else {
            player.setGameMode(GameMode.SPECTATOR);
            message = new TextComponent("You are now spectating");
        }

        message.setColor(ChatColor.GREEN);
        ctx.getSender().sendMessage(message);
    }
}
