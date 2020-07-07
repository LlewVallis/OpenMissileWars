package org.astropeci.omw.listeners;

import org.bukkit.GameMode;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemDropPreventer implements Listener {

    private boolean ignoreNextEvent = false;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked().getGameMode() == GameMode.SURVIVAL) {
            e.setResult(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (ignoreNextEvent) {
            ignoreNextEvent = false;
            return;
        }

        if (e.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            e.setCancelled(true);
            e.getPlayer().updateInventory();
        }
    }

    // Fix for a bug where vanilla /give can grant an extra item if the drop event is cancelled
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String message = e.getMessage();

        int space = message.indexOf(' ');
        if (space <= 0) {
            return;
        }

        String label = message.substring(1, space);

        if (label.equals("give") || label.equals("minecraft:give")) {
            ignoreNextEvent = true;
        }
    }
}
