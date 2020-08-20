package org.astropeci.omw.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.commandbuilder.*;
import org.astropeci.omw.worlds.ArenaPool;
import org.astropeci.omw.worlds.NamedArena;
import org.astropeci.omw.worlds.NoSuchArenaException;
import org.bukkit.Bukkit;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

public class ResetArenaCommand {

    private final Plugin plugin;
    private final ArenaPool arenaPool;

    private final TabExecutor executor;

    public ResetArenaCommand(Plugin plugin, ArenaPool arenaPool) {
        this.plugin = plugin;
        this.arenaPool = arenaPool;

        executor = new CommandBuilder().build(new ReflectionCommandCallback(this));
    }

    public void register(Plugin plugin) {
        CommandBuilder.registerCommand(
                plugin,
                "reset",
                "Instantly end the current arena and start a new one",
                "reset",
                "omw.team.reset",
                executor
        );
    }

    @PlayerOnlyCommand
    @ExecuteCommand
    public boolean execute(CommandContext ctx) {
        Optional<NamedArena> arenaOptional = arenaPool.getPlayerArena((Player) ctx.sender);

        if (arenaOptional.isEmpty()) {
            TextComponent message = new TextComponent("You must be in an arena to reset it");
            message.setColor(ChatColor.RED);
            ctx.sender.sendMessage(message);
            return true;
        }

        NamedArena arena = arenaOptional.get();

        boolean wasRunning = arena.arena.processWinner(Optional.empty());

        if (wasRunning) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                try {
                    arenaPool.resetArena(arena.name);
                } catch (NoSuchArenaException ex) {
                    throw new IllegalStateException(ex);
                }
            }, 20);
        }

        return true;
    }
}
