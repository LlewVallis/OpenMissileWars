package org.astropeci.omw.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

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

    public boolean doesPlayerHaveTeam(Player player) {
        return scoreboard.getEntryTeam(player.getName()) != null;
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
