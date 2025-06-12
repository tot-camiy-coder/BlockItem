package net.blockItem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class BlockItem extends JavaPlugin implements Listener {

    private List<Material> blocks = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        List<String> strItems = getConfig().getStringList("block-items");
        for (String s : strItems)
        {
            Material mat = Material.matchMaterial(s.toUpperCase());
            if (mat == null) return;

            blocks.add(mat);
        }

        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onCraft(CraftItemEvent e)
    {
        if (blocks.contains(Objects.requireNonNull(Objects.requireNonNull(e.getCurrentItem()).getType())))
        {
            e.setCancelled(true);
            e.getWhoClicked().sendMessage("§c Вы не можете скрафтить этот предмет!");
        }
    }

    @EventHandler
    public void onPickUp(EntityPickupItemEvent e)
    {
        if (blocks.contains(e.getItem().getItemStack().getType()))
        {
            e.setCancelled(true);
            e.getItem().remove();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {removeBlockItemFromPlayer(e.getPlayer());}
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {removeBlockItemFromPlayer(e.getPlayer());}
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {removeBlockItemFromPlayer(e.getPlayer());}
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {removeBlockItemFromPlayer(e.getPlayer());}
    @EventHandler
    public void onPlayerInventory(InventoryOpenEvent e) {removeBlockItemFromPlayer((Player) e.getPlayer());}
    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent e) {removeBlockItemFromPlayer(e.getPlayer()); e.setCancelled(true); }
    @EventHandler
    public void onPlayerScroll(PlayerItemHeldEvent e) {removeBlockItemFromPlayer(e.getPlayer());}




    private void removeBlockItemFromPlayer(Player e)
    {
        ItemStack[] contents = e.getInventory().getContents();
        ItemStack[] extraContents = e.getInventory().getExtraContents();
        ItemStack[] armorContents = e.getInventory().getArmorContents();
        ItemStack[] storageContents = e.getInventory().getStorageContents();

        removeItemFromList(e, contents);
        removeItemFromList(e, extraContents);
        removeItemFromList(e, armorContents);
        removeItemFromList(e, storageContents);
    }
    private void removeItemFromList(Player e, ItemStack[] items)
    {
        for (ItemStack item : items)
        {
            if (item == null) continue;
            if (blocks.contains(item.getType()))
            {
                e.getInventory().removeItem(item);
            }
        }
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
