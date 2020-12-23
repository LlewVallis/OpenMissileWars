package org.astropeci.omw.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ObsidianBreakPreventer implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.SURVIVAL && e.getBlock().getType() == Material.OBSIDIAN) {
             e.setCancelled(true);
        }
    }
}
