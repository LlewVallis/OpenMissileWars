package org.astropeci.omw.item;

import org.astropeci.omw.teams.GameTeam;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Set;

public class EquipmentProvider {

    public void giveToPlayer(Player player, GameTeam team) {
        Color color = team == GameTeam.GREEN ? Color.LIME : Color.RED;

        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

        Set<ItemStack> armourPieces = Set.of(chestplate, leggings, boots);

        for (ItemStack armourPiece : armourPieces) {
            ItemMeta meta = armourPiece.getItemMeta();
            ((LeatherArmorMeta) meta).setColor(color);
            meta.setUnbreakable(true);
            armourPiece.setItemMeta(meta);
        }

        ItemStack bow = new ItemStack(Material.BOW);

        ItemMeta bowMeta = bow.getItemMeta();
        bowMeta.setDisplayName(ChatColor.RESET + "GunBlade");
        bow.setItemMeta(bowMeta);

        bow.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 1);
        bow.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 4);
        bow.addUnsafeEnchantment(Enchantment.DURABILITY, 10);

        PlayerInventory inventory = player.getInventory();

        inventory.setChestplate(chestplate);
        inventory.setLeggings(leggings);
        inventory.setBoots(boots);

        inventory.addItem(bow);
    }
}
