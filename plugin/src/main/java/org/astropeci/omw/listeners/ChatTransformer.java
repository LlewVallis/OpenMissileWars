package org.astropeci.omw.listeners;

import lombok.RequiredArgsConstructor;
import org.astropeci.omw.teams.GameTeam;
import org.astropeci.omw.teams.GlobalTeamManager;
import org.astropeci.omw.worlds.ArenaPool;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@RequiredArgsConstructor
public class ChatTransformer implements Listener {

    private final GlobalTeamManager globalTeamManager;
    private final ArenaPool arenaPool;

    @EventHandler
    private void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        String worldName = arenaPool.getPlayerArena(player)
                .map(arena -> arena.name)
                .orElse("hub");

        String playerColor = globalTeamManager.getPlayerTeam(player)
                .map(team -> team == GameTeam.GREEN ? ChatColor.GREEN : ChatColor.RED)
                .orElse(ChatColor.AQUA)
                .toString();

        if (player.getGameMode() == GameMode.SPECTATOR) {
            playerColor += ChatColor.ITALIC;
        }

        String arrowColor = ChatColor.DARK_GRAY.toString() + ChatColor.BOLD;

        e.setFormat(ChatColor.GRAY + worldName + arrowColor + "> " + playerColor + "%s" + arrowColor + "> " + ChatColor.RESET + "%s");
    }

}
