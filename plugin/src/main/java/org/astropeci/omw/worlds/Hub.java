package org.astropeci.omw.worlds;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class Hub {

    private final WorldManager worldManager;

    public void sendPlayer(Player player) {
        worldManager.send(player, worldManager.getDefaultWorld());
    }
}
