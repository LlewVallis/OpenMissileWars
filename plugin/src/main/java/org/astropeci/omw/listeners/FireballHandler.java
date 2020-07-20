package org.astropeci.omw.listeners;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class FireballHandler implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Fireball) {
            Fireball fireball = (Fireball) event.getEntity();

            if (fireball.getVehicle() instanceof ArmorStand) {
                ArmorStand fireballHolder = (ArmorStand) fireball.getVehicle();
                fireball.leaveVehicle();
                fireballHolder.remove();
            }
        }
    }
}
