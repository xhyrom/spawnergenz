package me.xhyrom.spawnergenz.ui;

import me.xhyrom.spawnergenz.SpawnerGenz;
import me.xhyrom.spawnergenz.managers.SpawnerManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class ActionsUI implements Listener {
    private Inventory inventory;
    private CreatureSpawner spawner;
    private int count;

    public ActionsUI(CreatureSpawner spawner) {
        this.spawner = spawner;
        this.count = SpawnerManager.getSpawnerCount(spawner);
    }

    public void open(Player player) {
        this.inventory = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(
        count + "x " +
                spawner.getSpawnedType().name().substring(0, 1) +
                spawner.getSpawnedType().name().substring(1).toLowerCase()
        ));

        ItemStack storage = new ItemStack(Material.CHEST);
        storage.getItemMeta().displayName(MiniMessage.miniMessage().deserialize(
                "Open storage"
        ));

        this.inventory.setItem(
                10,
                storage
        );

        Bukkit.getPluginManager().registerEvents(this, SpawnerGenz.getInstance());

        player.openInventory(inventory);
    }

    public void close(Player player) {
        HandlerList.unregisterAll(this);
    }

    public void click(Player player, int slot) {
        switch (slot) {
            case 10:
                new StorageUI(spawner).open(player);
                break;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() != inventory) return;

        event.setCancelled(true);
        event.setResult(Event.Result.DENY);

        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getType().isAir()) return;

        click((Player) event.getWhoClicked(), event.getRawSlot());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory() != inventory) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        if (event.getInventory() != inventory) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() != inventory) return;
        close((Player) event.getPlayer());
    }
}
