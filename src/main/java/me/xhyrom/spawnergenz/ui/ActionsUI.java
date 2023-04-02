package me.xhyrom.spawnergenz.ui;

import me.xhyrom.spawnergenz.SpawnerGenz;
import me.xhyrom.spawnergenz.structs.Spawner;
import me.xhyrom.spawnergenz.utils.Utils;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class ActionsUI implements Listener {
    private Inventory inventory;
    private Spawner spawner;

    public ActionsUI(Spawner spawner) {
        this.spawner = spawner;
    }

    public void open(Player player) {
        this.inventory = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(
        spawner.getCount() + "x " +
                spawner.getCreatureSpawner().getSpawnedType().name().substring(0, 1) +
                spawner.getCreatureSpawner().getSpawnedType().name().substring(1).toLowerCase()
        ));

        ItemStack storage = new ItemStack(Material.valueOf(
                SpawnerGenz.getInstance().config.getString("ui.actions.open-storage.material")
        ));
        ItemMeta storageMeta = storage.getItemMeta();

        storageMeta.displayName(MiniMessage.miniMessage().deserialize(
                SpawnerGenz.getInstance().config.getString("ui.actions.open-storage.name")
        ).decoration(TextDecoration.ITALIC, false));
        storageMeta.lore(
                spawner.getStorage()
                        .stream()
                        .collect(Collectors.groupingBy(ItemStack::getType, Collectors.reducing(0, ItemStack::getAmount, Integer::sum)))
                        .entrySet()
                        .stream()
                        .map(entry ->
                                MiniMessage.miniMessage().deserialize(
                                        SpawnerGenz.getInstance().config.getString("ui.actions.open-storage.lore"),
                                        Placeholder.parsed("count", Utils.formatNumber(entry.getValue())),
                                        Placeholder.parsed("itemstack", Utils.convertUpperSnakeCaseToPascalCase(entry.getKey().name()))
                                ).decoration(TextDecoration.ITALIC, false)
                        )
                        .collect(Collectors.toList())
        );
        storage.setItemMeta(storageMeta);

        this.inventory.setItem(
                10,
                storage
        );

        ItemStack spawnerInfo = new ItemStack(Material.valueOf(
                SpawnerGenz.getInstance().config.getString("ui.actions.spawner-info.material")
        ));
        ItemMeta spawnerInfoMeta = spawnerInfo.getItemMeta();

        spawnerInfoMeta.displayName(MiniMessage.miniMessage().deserialize(
                SpawnerGenz.getInstance().config.getString("ui.actions.spawner-info.name")
        ).decoration(TextDecoration.ITALIC, false));
        spawnerInfoMeta.lore(
                SpawnerGenz.getInstance().config.getStringList("ui.actions.spawner-info.lore")
                        .stream()
                        .map(
                                line -> MiniMessage.miniMessage().deserialize(
                                        line,
                                        Placeholder.parsed("count", Utils.formatNumber(spawner.getCount())),
                                        Placeholder.parsed("experience", String.valueOf(spawner.getExperience())),
                                        Placeholder.parsed("spawner_type", Utils.convertUpperSnakeCaseToPascalCase(
                                                spawner.getCreatureSpawner().getSpawnedType().name()
                                        )),
                                        Placeholder.parsed("storage", String.valueOf(spawner.getStorage().size())),
                                        Placeholder.parsed("storage_max", String.valueOf(18 * spawner.getCount())),
                                        Placeholder.parsed("storage_percentage", String.format("%.1f", (double) spawner.getStorage().size() / (18 * spawner.getCount()) * 100))
                                ).decoration(TextDecoration.ITALIC, false)
                        )
                        .collect(Collectors.toList())
        );
        spawnerInfo.setItemMeta(spawnerInfoMeta);

        this.inventory.setItem(
                13,
                spawnerInfo
        );

        ItemStack collectXp = new ItemStack(Material.valueOf(
                SpawnerGenz.getInstance().config.getString("ui.actions.collect-xp.material")
        ));
        ItemMeta collectXpMeta = collectXp.getItemMeta();

        collectXpMeta.displayName(MiniMessage.miniMessage().deserialize(
                SpawnerGenz.getInstance().config.getString("ui.actions.collect-xp.name")
        ).decoration(TextDecoration.ITALIC, false));
        collectXpMeta.lore(
                SpawnerGenz.getInstance().config.getStringList("ui.actions.collect-xp.lore")
                        .stream()
                        .map(
                            line -> MiniMessage.miniMessage().deserialize(
                                    line,
                                    Placeholder.parsed("experience", String.valueOf(spawner.getExperience()))
                            ).decoration(TextDecoration.ITALIC, false)
                        )
                        .collect(Collectors.toList())
        );
        collectXp.setItemMeta(collectXpMeta);

        this.inventory.setItem(
                16,
                collectXp
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
            case 16:
                player.giveExp(spawner.getExperience());
                spawner.setExperience(0);

                player.sendMessage(
                        MiniMessage.miniMessage().deserialize(
                                SpawnerGenz.getInstance().config.getString("messages.collect-xp"),
                                Placeholder.parsed("experience", String.valueOf(spawner.getExperience()))
                        )
                );
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
