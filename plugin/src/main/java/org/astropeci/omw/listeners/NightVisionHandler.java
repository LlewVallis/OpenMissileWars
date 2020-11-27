package org.astropeci.omw.listeners;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NightVisionHandler implements Listener {

    private final Set<UUID> nightVisionExclusions = new HashSet<>();

    private void giveNightVision(Player player) {
        if (nightVisionExclusions.contains(player.getUniqueId())) {
            return;
        }

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.NIGHT_VISION,
                Integer.MAX_VALUE,
                0,
                true,
                false
        ));
    }

    public boolean toggleNightVision(Player player) {
        UUID uuid = player.getUniqueId();

        if (nightVisionExclusions.contains(uuid)) {
            nightVisionExclusions.remove(uuid);
            giveNightVision(player);
        } else {
            nightVisionExclusions.add(uuid);
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }

        return !nightVisionExclusions.contains(uuid);
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
