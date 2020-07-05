package org.astropeci.omw.teams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Optional;

public class GlobalTeamManager {

    private Scoreboard scoreboard;
    private Team green;
    private Team red;

    public void configScoreboard() {
        scoreboard = Bukkit.getServer().getScoreboardManager().getMainScoreboard();

        green = getOrRegisterTeam("green");
        red = getOrRegisterTeam("red");

        configTeam(green, ChatColor.GREEN);
        configTeam(red, ChatColor.RED);
    }

    private Team getOrRegisterTeam(String name) {
        Team team = scoreboard.getTeam(name);
        if (team == null) {
            team = scoreboard.registerNewTeam(name);
        }

        return team;
    }

    private void configTeam(Team team, ChatColor color) {
        team.setAllowFriendlyFire(false);
        team.setCanSeeFriendlyInvisibles(true);
        team.setColor(color);
        team.setPrefix(color.toString());
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OTHER_TEAMS);
    }

    public boolean isPlayerOnTeam(Player player, GameTeam team) {
        return getTeam(team).hasEntry(player.getName());
    }

    public Optional<GameTeam> getPlayerTeam(Player player) {
        Team team = scoreboard.getEntryTeam(player.getName());

        if (green.equals(team)) {
            return Optional.of(GameTeam.GREEN);
        } else if (red.equals(team)) {
            return Optional.of(GameTeam.RED);
        } else {
            return Optional.empty();
        }
    }

    public boolean addPlayerToTeam(Player player, GameTeam team) {
        if (isPlayerOnTeam(player, team)) {
            return false;
        } else {
            removePlayerFromTeam(player);
            getTeam(team).addEntry(player.getName());
            return true;
        }
    }

    public boolean removePlayerFromTeam(Player player) {
        boolean changed = false;

        for (Team team : scoreboard.getTeams()) {
            changed |= team.removeEntry(player.getName());
        }

        return changed;
    }

    private Team getTeam(GameTeam team) {
        return team == GameTeam.GREEN ? green : red;
    }
}
