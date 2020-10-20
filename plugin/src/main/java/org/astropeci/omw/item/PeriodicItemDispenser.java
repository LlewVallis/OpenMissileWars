package org.astropeci.omw.item;

import com.destroystokyo.paper.Title;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.teams.GlobalTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PeriodicItemDispenser implements AutoCloseable {

    private static final int ITEM_DROP_DELAY = 20 * 15;

    private final World world;
    private final GlobalTeamManager globalTeamManager;
    private final Plugin plugin;

    private Set<ItemStack> items = null;

    private boolean shouldRun = true;

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

        items = getItemsFromConfig();
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

    @SneakyThrows({InvalidConfigurationException.class})
    private Set<ItemStack> getItemsFromConfig()
    {
        Set<ItemStack> set = new HashSet<ItemStack>();
        List<Map<?, ?>> items = plugin.getConfig().getMapList("items");
        for(Map<?, ?> i : items){
            var m = (Map<String, ?>) i;
            ItemStack item;
            switch ((String)m.get("type")){
                case "simpleItem":
                    item = simpleItem(Material.getMaterial((String) m.get("material")), (String)m.get("name"));
                    break;
                case "itemStack":
                    item = new ItemStack(Objects.requireNonNull(Material.getMaterial((String) m.get("material"))), (int)m.get("amount"));
                    break;
                default:
                    throw new InvalidConfigurationException("Item type has to be either 'simpleItem' or 'itemStack'");
            }

            set.add(item);
        }
        return set;
    }

    private ItemStack simpleItem(Material material, String name) {
        ItemStack stack = new ItemStack(material);

        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + name);
        stack.setItemMeta(meta);

        return stack;
    }

    @Override
    public void close() {
        shouldRun = false;
    }
}
