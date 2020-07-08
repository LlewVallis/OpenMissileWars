package org.astropeci.omw.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.commandbuilder.*;
import org.astropeci.omw.worlds.ArenaPool;
import org.astropeci.omw.worlds.NamedArena;
import org.bukkit.GameMode;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

public class SpectateCommand {

    private final ArenaPool arenaPool;

    private final TabExecutor executor;

    public SpectateCommand(ArenaPool arenaPool) {
        executor = new CommandBuilder().build(new ReflectionCommandCallback(this));

        this.arenaPool = arenaPool;
    }

    public void register(Plugin plugin) {
        CommandBuilder.registerCommand(
                plugin,
                "sp",
                "Spectate a game in an arena",
                "sp",
                "omw.spectate",
                executor
        );
    }

    @PlayerOnlyCommand
    @ExecuteCommand
    public boolean execute(CommandContext ctx) {
        Player player = (Player) ctx.sender;

        Optional<NamedArena> arenaOptional = arenaPool.getPlayerArena(player);

        if (arenaOptional.isEmpty()) {
            TextComponent message = new TextComponent("You must be in an arena to spectate");
            message.setColor(ChatColor.RED);
            ctx.sender.sendMessage(message);
            return true;
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
        ctx.sender.sendMessage(message);

        return true;
    }
}
