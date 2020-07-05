package org.astropeci.omw.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.commandbuilder.*;
import org.astropeci.omw.teams.GameTeam;
import org.astropeci.omw.teams.GlobalTeamManager;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class JoinTeamCommand {

    private final GlobalTeamManager globalTeamManager;

    private final TabExecutor executor;

    public JoinTeamCommand(GlobalTeamManager globalTeamManager) {
        executor = new CommandBuilder()
                .addArgument(new TeamArgument().optional())
                .build(new ReflectionCommandCallback(this));

        this.globalTeamManager = globalTeamManager;
    }

    public void register(Plugin plugin) {
        CommandBuilder.registerCommand(
                plugin,
                "team-join",
                "Join a team or leave your current one",
                "team-join [green|red]",
                "omw.team.join",
                executor
        );
    }

    @PlayerOnlyCommand
    @ExecuteCommand
    public boolean execute(CommandContext ctx, GameTeam team) {
        TextComponent message;

        if (team == null) {
            boolean changed = globalTeamManager.removePlayerFromTeam((Player) ctx.sender);

            if (changed) {
                message = new TextComponent("You are no longer in a team");
                message.setColor(ChatColor.GREEN);
            } else {
                message = new TextComponent("You didn't have a team to leave");
                message.setColor(ChatColor.RED);
            }
        } else {
            boolean changed = globalTeamManager.addPlayerToTeam((Player) ctx.sender, team);

            if (changed) {
                message = new TextComponent("Joined " + team + " team");
                message.setColor(ChatColor.GREEN);
            } else {
                message = new TextComponent("You were already on " + team + " team");
                message.setColor(ChatColor.RED);
            }
        }

        ctx.sender.sendMessage(message);

        return true;
    }
}
