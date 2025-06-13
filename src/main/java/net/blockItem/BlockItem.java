package net.blockItem;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class BlockItem extends JavaPlugin implements Listener {

    private final Set<Material> blocks = new HashSet<>();
    private String block_craft = "";
    private String block_item = "";

    @Override
    public void onEnable() {
        // cfg
        saveDefaultConfig();
        block_craft = ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("messages.block-craft", "&cВы не можете скрафтить данный предмет!"));
        block_item = ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("messages.block-item", "&cУ вас запрешённый предмет!"));

        // update blocks
        List<String> strItems = getConfig().getStringList("block-items");
        for (String s : strItems) {
            Material mat = Material.matchMaterial(s.toUpperCase());
            if (mat == null) {
                getLogger().warning("Invalid material in config: " + s);
                continue;
            }
            blocks.add(mat);
        }
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("blockitem").setTabCompleter(this);
    }


    // CRAFT
    @EventHandler
    public void onCraft(CraftItemEvent e) {
        ItemStack current = e.getCurrentItem();
        if (current != null && blocks.contains(current.getType())) {
            e.setCancelled(true);
            e.getWhoClicked().sendMessage(block_craft);
        }
    }

    // PICKUP
    @EventHandler
    public void onPickUp(EntityPickupItemEvent e) {
        if (blocks.contains(e.getItem().getItemStack().getType())) {
            e.setCancelled(true);
            e.getItem().remove();
        }
    }

    // PLAYER
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        removeBlockItemFromPlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        removeBlockItemFromPlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        removeBlockItemFromPlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        removeBlockItemFromPlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent e) {
        removeBlockItemFromPlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent e) {
        removeBlockItemFromPlayer(e.getPlayer());
        if (blocks.contains(e.getItemDrop().getItemStack().getType())) {
            e.setCancelled(true);
            e.getItemDrop().remove();
        }
    }


    // INVENTORY
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            if (removeBlockItemFromPlayer(p)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryInteractEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            if (removeBlockItemFromPlayer(p)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            if (removeBlockItemFromPlayer(p)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent e) {
        if (e.getSource() instanceof Player p) {
            if (removeBlockItemFromPlayer(p)) {
                e.setCancelled(true);
            }
        }
    }





    private boolean removeBlockItemFromPlayer(Player player) {
        if (player.hasPermission("blockitem.bypass")) return false;

        boolean removed = false;
        ItemStack[] allItems = player.getInventory().getContents();
        ItemStack[] extraItems = player.getInventory().getExtraContents();
        ItemStack[] armorItems = player.getInventory().getArmorContents();
        ItemStack[] storageItems = player.getInventory().getStorageContents();

        Set<ItemStack> toRemove = new HashSet<>();

        collectItemsToRemove(toRemove, allItems);
        collectItemsToRemove(toRemove, extraItems);
        collectItemsToRemove(toRemove, armorItems);
        collectItemsToRemove(toRemove, storageItems);

        if (!toRemove.isEmpty()) {
            player.getInventory().removeItem(toRemove.toArray(new ItemStack[0]));
            removed = true;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand != null && blocks.contains(mainHand.getType())) {
            player.getInventory().setItemInMainHand(null);
            removed = true;
        }

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand != null && blocks.contains(offHand.getType())) {
            player.getInventory().setItemInOffHand(null);
            removed = true;
        }

        if (removed) {
            player.updateInventory();
            player.sendMessage(block_item);
        }

        return removed;
    }

    private void collectItemsToRemove(Set<ItemStack> toRemove, ItemStack[] items) {
        for (ItemStack item : items) {
            if (item != null && blocks.contains(item.getType())) {
                toRemove.add(item);
            }
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player p)) return true;
        if (label.equals("blockitem"))
        {
            String help = ChatColor.translateAlternateColorCodes('&',
                    getConfig().getString("messages.help", "empty config"));
            String no_perm = ChatColor.translateAlternateColorCodes('&',
                    getConfig().getString("messages.no-permission", "empty config"));
            String hand_empty = ChatColor.translateAlternateColorCodes('&',
                    getConfig().getString("messages.hand-empty", "empty config"));
            String blocks_list = ChatColor.translateAlternateColorCodes('&',
                    getConfig().getString("messages.blocks-list", "empty config"));
            String added = ChatColor.translateAlternateColorCodes('&',
                    getConfig().getString("messages.added", "empty config"));
            String removed = ChatColor.translateAlternateColorCodes('&',
                    getConfig().getString("messages.removed", "empty config"));
            String already = ChatColor.translateAlternateColorCodes('&',
                    getConfig().getString("messages.already", "empty config"));
            if (args.length < 1)
            {
                p.sendMessage(help);
                return true;
            }
            else {
                String mode = args[0];
                Material item = p.getInventory().getItemInMainHand().getType();

                switch (mode)
                {
                    case "add":
                        if (!p.hasPermission("blockitem.add"))
                        {
                            p.sendMessage(no_perm);
                            return true;
                        }
                        if (item == null || item == Material.AIR)
                        {
                            p.sendMessage(hand_empty);
                            return true;
                        }
                        if (blocks.contains(item))
                        {
                            p.sendMessage(already);
                            return true;
                        }
                        blocks.add(item);
                        getConfig().set("block-items", blocks.stream().map(Material::name).toList());
                        saveConfig();
                        p.sendMessage(added);
                        break;
                    case "remove":
                        if (!p.hasPermission("blockitem.remove"))
                        {
                            p.sendMessage(no_perm);
                            return true;
                        }
                        if (item == null || item == Material.AIR)
                        {
                            p.sendMessage(hand_empty);
                            return true;
                        }
                        if (blocks.contains(item))
                        {
                            p.sendMessage(already);
                            return true;
                        }
                        blocks.remove(item);
                        getConfig().set("block-items", blocks.stream().map(Material::name).toList());
                        saveConfig();
                        p.sendMessage(removed);
                        break;
                    case "list":
                        if (!p.hasPermission("blockitem.list"))
                        {
                            p.sendMessage(no_perm);
                            return true;
                        }
                        p.sendMessage(blocks_list);
                        for (Material s : blocks)
                        {
                            p.sendMessage("  - "+s.name());
                        }
                        break;

                }
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (String sub : Arrays.asList("add", "remove", "list")) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    if (!sender.hasPermission("blocktime.bypass")) {
                        continue;
                    }
                    completions.add(sub);
                }
            }
            return completions;
        }
        return List.of();
    }

    @Override
    public void onDisable() {
        // Логика при выключении плагина
    }
}
