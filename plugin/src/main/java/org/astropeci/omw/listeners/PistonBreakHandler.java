package org.astropeci.omw.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class PistonBreakHandler implements Listener {

    private final Plugin plugin;

    private final Object lock = new Object();

    private final Map<Player, BlockBreakStatus> playerBreakingStatuses = new HashMap<>();
    private final AtomicInteger currentTick = new AtomicInteger();

    private static final int MAXIMUM_BREAKING_PAUSE = 1;
    private static final int BREAK_SPEED = 20;

    @AllArgsConstructor
    private static class BlockBreakStatus {

        public Block block;
        public int lastTickClicked;
        public int ticksOfProgress;
    }

    private class PacketListener extends PacketAdapter {

        public PacketListener() {
            super(PistonBreakHandler.this.plugin, PacketType.Play.Client.ARM_ANIMATION);
        }

        @Override
        public void onPacketReceiving(PacketEvent e) {
            Player player = e.getPlayer();
            synchronized (lock) {
                onArmSwing(player);
            }
        }

        private void onArmSwing(Player player) {
            Block targetedBlock = getTargetedBlock(player);
            int currentTick = PistonBreakHandler.this.currentTick.get();

            BlockBreakStatus status = playerBreakingStatuses.get(player);
            if (status == null) {
                if (targetedBlock != null) {
                    BlockBreakStatus newStatus = new BlockBreakStatus(targetedBlock, currentTick, 1);
                    playerBreakingStatuses.put(player, newStatus);
                }

                return;
            }

            long timeSinceLastAnimation = currentTick - status.lastTickClicked;
            status.lastTickClicked = currentTick;

            if (targetedBlock == null) {
                status.ticksOfProgress = 0;
                return;
            }

            if (!targetedBlock.equals(status.block)) {
                BlockBreakStatus newStatus = new BlockBreakStatus(targetedBlock, currentTick, 1);
                playerBreakingStatuses.put(player, newStatus);
                return;
            }

            if (timeSinceLastAnimation > MAXIMUM_BREAKING_PAUSE + 1) {
                status.ticksOfProgress = 0;
                return;
            }

            status.ticksOfProgress += timeSinceLastAnimation;

            Material material = targetedBlock.getType();
            boolean isPiston = material == Material.PISTON || material == Material.STICKY_PISTON || material == Material.PISTON_HEAD;

            if (status.ticksOfProgress >= BREAK_SPEED && isPiston) {
                playerBreakingStatuses.remove(player);

                targetedBlock.getWorld().playSound(targetedBlock.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 0.8f);
                Bukkit.getScheduler().runTask(plugin, (Runnable) targetedBlock::breakNaturally);
            }
        }

        private Block getTargetedBlock(Player player) {
            return player.getTargetBlockExact(5);
        }
    }

    public void register() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener());
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onTick(ServerTickStartEvent e) {
        currentTick.incrementAndGet();
    }
}
