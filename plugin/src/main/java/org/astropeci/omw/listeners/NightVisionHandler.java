package org.astropeci.omw.listeners;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NightVisionHandler implements Listener {

    private void giveNightVision(Player player) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.NIGHT_VISION,
                Integer.MAX_VALUE,
                0
        ));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        giveNightVision(e.getPlayer());
    }

    @EventHandler
    public void onPostRespawn(PlayerPostRespawnEvent e) {
        giveNightVision(e.getPlayer());
    }
}
