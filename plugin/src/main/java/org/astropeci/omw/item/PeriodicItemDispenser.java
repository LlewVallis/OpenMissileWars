package org.astropeci.omw.item;

import com.destroystokyo.paper.Title;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.settings.ArenaSettings;
import org.astropeci.omw.settings.ItemSpec;
import org.astropeci.omw.teams.GlobalTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PeriodicItemDispenser implements AutoCloseable {

    private static final int ITEM_DROP_DELAY = 20 * 15;

    private final ArenaSettings settings;
    private final World world;
    private final GlobalTeamManager globalTeamManager;
    private final Plugin plugin;

    private boolean shouldRun = true;

    public PeriodicItemDispenser(ArenaSettings settings, World world, GlobalTeamManager globalTeamManager, Plugin plugin) {
        this.settings = settings;
        this.world = world;
        this.globalTeamManager = globalTeamManager;
        this.plugin = plugin;
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

        dispense();

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this::giveItemsPeriodically, ITEM_DROP_DELAY);
    }

    public void dispense() {
        Set<Player> players = world.getPlayers().stream()
                .filter(player -> globalTeamManager.getPlayerTeam(player).isPresent())
                .filter(player -> player.getGameMode() == GameMode.SURVIVAL)
                .collect(Collectors.toSet());

        Set<ItemSpec> itemSpecs = settings.getItemSpecs();
        List<ItemStack> itemStacks = itemSpecs.stream()
                .map(this::itemSpecToStack)
                .collect(Collectors.toCollection(ArrayList::new));

        Collections.shuffle(itemStacks);

        if (!itemStacks.isEmpty()) {
            ItemStack item = itemStacks.get(0);
            for (Player player : players) {
                giveItem(player, item);
            }
        }
    }

    private ItemStack itemSpecToStack(ItemSpec spec) {
        ItemStack stack = new ItemStack(spec.getMaterial());
        ItemMeta meta = stack.getItemMeta();

        stack.setAmount(spec.getAmount());
        if (spec.getName() != null) meta.setDisplayName(ChatColor.RESET + spec.getName());

        stack.setItemMeta(meta);
        return stack;
    }

    private void giveItem(Player player, ItemStack item) {
        PlayerInventory inventory = player.getInventory();

        if (!inventory.contains(item.getType()) && inventory.getItemInOffHand().getType() != item.getType()) {
            inventory.addItem(item);
        } else {
            for (ItemStack search : inventory.getContents()) {
                if (search != null && search.getType() == item.getType()) {
                    if (search.getAmount() < item.getAmount()) {
                        search.setAmount(item.getAmount());
                        return;
                    }
                }
            }

            String name = item.getItemMeta().getDisplayName();
            if (name.isBlank()) {
                name = defaultItemName(item.getType());
            }

            if (item.getAmount() > 1) {
                name += "s";
            }

            player.sendActionBar(ChatColor.AQUA + name + " already obtained!");
        }
    }

    private String defaultItemName(Material material) {
        StringBuilder result = new StringBuilder();
        boolean nextCharShouldBeCapitalised = true;

        for (char chr : material.name().toCharArray()) {
            if (nextCharShouldBeCapitalised) {
                result.append(Character.toUpperCase(chr));
            } else {
                result.append(Character.toLowerCase(chr));
            }

            nextCharShouldBeCapitalised = Character.isSpaceChar(chr);
        }

        return result.toString();
    }

    @Override
    public void close() {
        shouldRun = false;
    }
}
