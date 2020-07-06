package org.astropeci.omw.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.commandbuilder.*;
import org.astropeci.omw.teams.GameTeam;
import org.astropeci.omw.teams.GlobalTeamManager;
import org.astropeci.omw.worlds.ArenaPool;
import org.astropeci.omw.worlds.WorldManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

public class JoinTeamCommand {

    private final GameTeam team;
    private final GlobalTeamManager globalTeamManager;
    private final ArenaPool arenaPool;
    private final WorldManager worldManager;

    private final TabExecutor executor;

    public JoinTeamCommand(GameTeam team, GlobalTeamManager globalTeamManager, ArenaPool arenaPool, WorldManager worldManager) {
        executor = new CommandBuilder().build(new ReflectionCommandCallback(this));

        this.team = team;
        this.globalTeamManager = globalTeamManager;
        this.arenaPool = arenaPool;
        this.worldManager = worldManager;
    }

    public void register(Plugin plugin) {
        CommandBuilder.registerCommand(
                plugin,
                team == GameTeam.GREEN ? "green" : "red",
                "Join a team in the arena you are currently in",
                team == GameTeam.GREEN ? "green" : "red",
                "omw.team.join",
                executor
        );
    }

    @PlayerOnlyCommand
    @ExecuteCommand
    public boolean execute(CommandContext ctx) {
        Player player = (Player) ctx.sender;
        Optional<NamedArena> arenaOptional = arenaPool.getPlayerArena(player);

        if (arenaOptional.isEmpty()) {
            TextComponent message = new TextComponent("You must be in an arena to join a team");
            message.setColor(ChatColor.RED);
            ctx.sender.sendMessage(message);
            return true;
        }

        NamedArena arena = arenaOptional.get();

        boolean changed = globalTeamManager.addPlayerToTeam(player, team);

        if (changed) {
            Location spawn = arena.arena.getSpawn(team);

            worldManager.send(player, spawn);
            // Sending clears it, so we need to do it again
            globalTeamManager.addPlayerToTeam(player, team);

            player.setGameMode(GameMode.SURVIVAL);
        }

        TextComponent message;
        if (changed) {
            message = new TextComponent("Joined " + team + " team");
            message.setColor(ChatColor.GREEN);
        } else {
            message = new TextComponent("You were already on " + team + " team");
            message.setColor(ChatColor.RED);
        }

        ctx.sender.sendMessage(message);

        return true;
    }
}
