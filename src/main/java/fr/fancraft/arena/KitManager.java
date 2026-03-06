package fr.fancraft.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.Arrays;

public class KitManager implements Listener {

    private static final String GUI_TITLE = ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Selecteur de Kit";

    public void openGui(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);

        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        inv.setItem(10, buildIcon(Material.IRON_SWORD, ChatColor.RED + "" + ChatColor.BOLD + "Guerrier",
                "",
                ChatColor.GRAY + " Epee en fer",
                ChatColor.GRAY + " Armure en fer complete",
                ChatColor.GRAY + " 3 golden apples",
                "",
                ChatColor.YELLOW + " Clic pour selectionner"));

        inv.setItem(12, buildIcon(Material.BOW, ChatColor.GREEN + "" + ChatColor.BOLD + "Archer",
                "",
                ChatColor.GRAY + " Arc Power I",
                ChatColor.GRAY + " 64 fleches + epee en pierre",
                ChatColor.GRAY + " Armure en chaine",
                ChatColor.GRAY + " 2 golden apples",
                "",
                ChatColor.YELLOW + " Clic pour selectionner"));

        inv.setItem(14, buildIcon(Material.DIAMOND_CHESTPLATE, ChatColor.AQUA + "" + ChatColor.BOLD + "Tank",
                "",
                ChatColor.GRAY + " Epee en pierre",
                ChatColor.GRAY + " Armure en diamant complete",
                ChatColor.GRAY + " 5 golden apples",
                "",
                ChatColor.YELLOW + " Clic pour selectionner"));

        inv.setItem(16, buildIcon(Material.GOLD_SWORD, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Berserker",
                "",
                ChatColor.GRAY + " Epee en diamant Tranchant II",
                ChatColor.GRAY + " Armure en cuir",
                ChatColor.GRAY + " 1 golden apple",
                "",
                ChatColor.YELLOW + " Clic pour selectionner"));

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getInventory().getTitle() == null) return;
        if (!event.getInventory().getTitle().equals(GUI_TITLE)) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        String kitName;
        switch (slot) {
            case 10: kitName = "Guerrier"; giveGuerrier(player); break;
            case 12: kitName = "Archer"; giveArcher(player); break;
            case 14: kitName = "Tank"; giveTank(player); break;
            case 16: kitName = "Berserker"; giveBerserker(player); break;
            default: return;
        }

        player.closeInventory();
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20f);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.setFireTicks(0);
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 0.8f, 1.2f);
        player.sendMessage(ChatColor.GRAY + "Kit " + ChatColor.GREEN + kitName + ChatColor.GRAY + " equipe.");
    }

    private void clearPlayer(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
    }

    private void giveGuerrier(Player player) {
        clearPlayer(player);
        player.getInventory().setItem(0, new ItemStack(Material.IRON_SWORD));
        player.getInventory().setItem(1, new ItemStack(Material.GOLDEN_APPLE, 3));

        player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
    }

    private void giveArcher(Player player) {
        clearPlayer(player);
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
        player.getInventory().setItem(0, bow);
        player.getInventory().setItem(1, new ItemStack(Material.STONE_SWORD));
        player.getInventory().setItem(2, new ItemStack(Material.GOLDEN_APPLE, 2));
        player.getInventory().setItem(9, new ItemStack(Material.ARROW, 64));

        player.getInventory().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
    }

    private void giveTank(Player player) {
        clearPlayer(player);
        player.getInventory().setItem(0, new ItemStack(Material.STONE_SWORD));
        player.getInventory().setItem(1, new ItemStack(Material.GOLDEN_APPLE, 5));

        player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
    }

    private void giveBerserker(Player player) {
        clearPlayer(player);
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
        player.getInventory().setItem(0, sword);
        player.getInventory().setItem(1, new ItemStack(Material.GOLDEN_APPLE, 1));

        player.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
    }

    private ItemStack buildIcon(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
}
