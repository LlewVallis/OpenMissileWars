package org.astropeci.omw.commands;

import io.github.llewvallis.commandbuilder.*;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.item.EquipmentProvider;
import org.astropeci.omw.teams.GameTeam;
import org.astropeci.omw.teams.GlobalTeamManager;
import org.astropeci.omw.worlds.ArenaPool;
import org.astropeci.omw.worlds.NamedArena;
import org.astropeci.omw.worlds.WorldManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.Optional;

@RequiredArgsConstructor
public class JoinTeamCommand {

    private final GameTeam team;
    private final GlobalTeamManager globalTeamManager;
    private final ArenaPool arenaPool;
    private final WorldManager worldManager;
    private final EquipmentProvider equipmentProvider;

    public void register(PluginCommand command) {
        new CommandBuilder().build(new ReflectionCommandCallback(this), command);
    }

    @ExecuteCommand
    @PlayerOnlyCommand
    public void execute(CommandContext ctx) {
        Player player = (Player) ctx.getSender();
        Optional<NamedArena> arenaOptional = arenaPool.getPlayerArena(player);

        if (arenaOptional.isEmpty()) {
            TextComponent message = new TextComponent("You must be in an arena to join a team");
            message.setColor(ChatColor.RED);
            ctx.getSender().sendMessage(message);
            return;
        }

        NamedArena arena = arenaOptional.get();

        boolean changed = globalTeamManager.addPlayerToTeam(player, team);

        if (changed) {
            Location spawn = arena.arena.getSpawn(team);

            worldManager.send(player, spawn);
            // Sending clears it, so we need to do it again
            globalTeamManager.addPlayerToTeam(player, team);

            player.setGameMode(GameMode.SURVIVAL);
            equipmentProvider.giveToPlayer(player, team);
        }

        String teamName = team == GameTeam.GREEN ? "green" : "red";

        TextComponent message;
        if (changed) {
            message = new TextComponent("Joined " + teamName + " team");
            message.setColor(ChatColor.GREEN);
        } else {
            message = new TextComponent("You were already on " + teamName + " team");
            message.setColor(ChatColor.RED);
        }

        ctx.getSender().sendMessage(message);
    }
}
