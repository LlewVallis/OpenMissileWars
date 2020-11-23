package org.astropeci.omw.item;

import com.destroystokyo.paper.Title;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.teams.GlobalTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

public class PeriodicItemDispenser implements AutoCloseable {

    private static final int ITEM_DROP_DELAY = 20 * 15;

    private final World world;
    private final GlobalTeamManager globalTeamManager;
    private final Plugin plugin;

    private final Set<ItemStack> items;

    private boolean shouldRun = true;

    public PeriodicItemDispenser(World world, GlobalTeamManager globalTeamManager, Plugin plugin) {
        this.world = world;
        this.globalTeamManager = globalTeamManager;
        this.plugin = plugin;

        items = getItemsFromConfig();
    }

    public void start() {
        if (shouldStart()) {
            TextComponent message = new TextComponent("Game is starting in 10 seconds");
            message.setColor(ChatColor.GREEN);
            world.getPlayers().forEach(player -> player.sendMessage(message));

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                TextComponent titleMessage;

                boolean shouldStart = shouldStart();
                if (shouldStart) {
                    titleMessage = new TextComponent("Game is starting");
                    titleMessage.setColor(ChatColor.GREEN);
                } else {
                    titleMessage = new TextComponent("Not enough players to start");
                    titleMessage.setColor(ChatColor.RED);
                }

                Title title = new Title(titleMessage, null, 5, 40, 20);

                world.getPlayers().forEach(player -> player.sendTitle(title));

                if (shouldStart) {
                    giveItemsPeriodically();
                } else {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this::start, 1);
                }
            }, 20 * 10);
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this::start, 1);
        }
    }

    private boolean shouldStart() {
        long occupiedTeams = world.getPlayers().stream()
                .filter(player -> player.getGameMode() == GameMode.SURVIVAL)
                .flatMap(player -> globalTeamManager.getPlayerTeam(player).stream())
                .distinct()
                .count();

        return occupiedTeams >= 1;
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

    @SneakyThrows({ InvalidConfigurationException.class })
    private Set<ItemStack> getItemsFromConfig() {
        Set<ItemStack> items = new HashSet<>();
        List<Map<?, ?>> itemConfigurations = plugin.getConfig().getMapList("items");

        for(Map<?, ?> itemConfiguration : itemConfigurations) {
            Object materialNameObject = getRequiredConfigProperty("material", itemConfiguration);
            if (!(materialNameObject instanceof String)) {
                throw new InvalidConfigurationException("item material was not a string");
            }

            String materialName = (String) materialNameObject;
            Material material = Material.getMaterial(materialName);

            if (material == null) {
                throw new InvalidConfigurationException("the material '" + materialName + "' does not exist");
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();

            Object amountObject = itemConfiguration.get("amount");
            if (amountObject instanceof Integer) {
                item.setAmount((Integer) amountObject);;
            } else if (amountObject != null) {
                throw new InvalidConfigurationException("item amount was not an integer");
            }

            Object nameObject = itemConfiguration.get("name");
            if (nameObject instanceof String) {
                meta.setDisplayName(ChatColor.RESET.toString() + nameObject);
            } else if (nameObject != null) {
                throw new InvalidConfigurationException("item name was not a string");
            }

            item.setItemMeta(meta);
            items.add(item);
        }

        return items;
    }

    @SneakyThrows({ InvalidConfigurationException.class })
    private Object getRequiredConfigProperty(String key, Map<?, ?> map) {
        Object result = map.get(key);
        if (result == null) {
            throw new InvalidConfigurationException("the config key '" + key + "' was missing");
        }

        return result;
    }

    @Override
    public void close() {
        shouldRun = false;
    }
}
