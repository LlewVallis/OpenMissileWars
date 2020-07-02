package org.astropeci.omw.worlds;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Hub {

    public static void sendPlayer(Player player) {
        World world = Bukkit.getWorld("world");
        Worlds.send(player, world);
    }
}
