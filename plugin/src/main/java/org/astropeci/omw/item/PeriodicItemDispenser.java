package org.astropeci.omw.item;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.teams.GlobalTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PeriodicItemDispenser implements AutoCloseable {

    private static final int ITEM_DROP_DELAY = 20 * 15;

    private final World world;
    private final GlobalTeamManager globalTeamManager;
    private final Plugin plugin;

    private final Set<ItemStack> items = Set.of(
            new ItemStack(Material.CREEPER_SPAWN_EGG),
            new ItemStack(Material.GHAST_SPAWN_EGG),
            new ItemStack(Material.WITCH_SPAWN_EGG),
            new ItemStack(Material.OCELOT_SPAWN_EGG),
            new ItemStack(Material.GUARDIAN_SPAWN_EGG),
            new ItemStack(Material.BLAZE_SPAWN_EGG),
            new ItemStack(Material.SNOWBALL),
            new ItemStack(Material.ARROW, 3)
    );

    private boolean shouldRun = true;

    public void start() {
        long occupiedTeams = world.getPlayers().stream()
                .filter(player -> player.getGameMode() == GameMode.SURVIVAL)
                .flatMap(player -> globalTeamManager.getPlayerTeam(player).stream())
                .distinct()
                .count();

        if (occupiedTeams == 1) {
            TextComponent message = new TextComponent("Game is starting in 10 seconds");
            message.setColor(ChatColor.GREEN);
            world.getPlayers().forEach(player -> player.sendMessage(message));

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this::giveItemsPeriodically, 20 * 10);
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this::start, 1);
        }
    }

    private void giveItemsPeriodically() {
        if (!shouldRun) {
            Bukkit.getLogger().info("Shutting down item dispenser");
            return;
        }

        Set<Player> players = world.getPlayers().stream()
                .filter(player -> globalTeamManager.getPlayerTeam(player).isPresent())
                .filter(player -> player.getGameMode() == GameMode.SURVIVAL)
                .collect(Collectors.toSet());

        List<ItemStack> itemList = new ArrayList<>(items);
        Collections.shuffle(itemList);

        ItemStack item = itemList.get(0);

        for (Player player : players) {
            PlayerInventory inventory = player.getInventory();

            if (!inventory.contains(item.getType()) && inventory.getItemInOffHand().getType() != item.getType()) {
                inventory.addItem(item);
            }
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this::giveItemsPeriodically, ITEM_DROP_DELAY);
    }

    @Override
    public void close() {
        shouldRun = false;
    }
}
