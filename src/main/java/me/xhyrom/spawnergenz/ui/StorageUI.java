package me.xhyrom.spawnergenz.ui;

import me.xhyrom.spawnergenz.SpawnerGenz;
import me.xhyrom.spawnergenz.hooking.Hooks;
import me.xhyrom.spawnergenz.structs.Placeholder;
import me.xhyrom.spawnergenz.structs.Spawner;
import me.xhyrom.spawnergenz.structs.actions.Action;
import me.xhyrom.spawnergenz.structs.actions.ActionOpportunity;
import me.xhyrom.spawnergenz.structs.actions.ActionStatus;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

public class StorageUI implements Listener {
    private Inventory inventory;
    private final Spawner spawner;
    private int page = 0;
    private final int maxPage;

    public StorageUI(Spawner spawner) {
        this.spawner = spawner;
        this.maxPage = (int) Math.ceil(spawner.getStorage().size() / 45.0);
    }

    public void open(Player player) {
        this.inventory = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(
                spawner.getCount() + "x " +
                        spawner.getCreatureSpawner().getSpawnedType().name().substring(0, 1) +
                        spawner.getCreatureSpawner().getSpawnedType().name().substring(1).toLowerCase()
        ));

        this.initializeItems();

        Bukkit.getPluginManager().registerEvents(this, SpawnerGenz.getInstance());

        player.openInventory(inventory);
    }

    private void initializeItems() {
        this.inventory.clear();

        for (int i = 0; i < 45; i++) {
            if (spawner.getStorage().size() > i + page * 45) {
                this.inventory.setItem(
                        i,
                        spawner.getStorage().get(i + page * 45)
                );
            }
        }

        ItemStack back = new ItemStack(Material.valueOf(
                SpawnerGenz.getInstance().getConfig().getString("ui.storage.back.material")
        ));
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MiniMessage.miniMessage().deserialize(
                SpawnerGenz.getInstance().getConfig().getString("ui.storage.back.name")
        ).decoration(TextDecoration.ITALIC, false));
        back.setItemMeta(backMeta);

        this.inventory.setItem(
                45,
                back
        );

        ItemStack previous = new ItemStack(Material.valueOf(
                SpawnerGenz.getInstance().getConfig().getString("ui.storage.previous-page.material")
        ));
        ItemStack collectLoot = new ItemStack(Material.valueOf(
                SpawnerGenz.getInstance().getConfig().getString("ui.storage.collect-loot.material")
        ));
        ItemStack next = new ItemStack(Material.valueOf(
                SpawnerGenz.getInstance().getConfig().getString("ui.storage.next-page.material")
        ));
        ItemStack sell = new ItemStack(Material.valueOf(
                SpawnerGenz.getInstance().getConfig().getString("ui.storage.sell-all.material")
        ));

        ItemMeta previousMeta = previous.getItemMeta();
        ItemMeta collectLootMeta = collectLoot.getItemMeta();
        ItemMeta nextMeta = next.getItemMeta();
        ItemMeta sellMeta = sell.getItemMeta();
        previousMeta.displayName(MiniMessage.miniMessage().deserialize(
                SpawnerGenz.getInstance().getConfig().getString("ui.storage.previous-page.name")
        ).decoration(TextDecoration.ITALIC, false));
        collectLootMeta.displayName(MiniMessage.miniMessage().deserialize(
                SpawnerGenz.getInstance().getConfig().getString("ui.storage.collect-loot.name")
        ).decoration(TextDecoration.ITALIC, false));
        nextMeta.displayName(MiniMessage.miniMessage().deserialize(
                SpawnerGenz.getInstance().getConfig().getString("ui.storage.next-page.name")
        ).decoration(TextDecoration.ITALIC, false));
        sellMeta.displayName(MiniMessage.miniMessage().deserialize(
                SpawnerGenz.getInstance().getConfig().getString("ui.storage.sell-all.name")
        ).decoration(TextDecoration.ITALIC, false));

        previous.setItemMeta(previousMeta);
        collectLoot.setItemMeta(collectLootMeta);
        next.setItemMeta(nextMeta);
        sell.setItemMeta(sellMeta);

        if (page > 0) {
            this.inventory.setItem(
                    48,
                    previous
            );
        }

        this.inventory.setItem(
                49,
                collectLoot
        );

        if (page <= maxPage) {
            this.inventory.setItem(
                    50,
                    next
            );
        }

        this.inventory.setItem(
                53,
                sell
        );
    }

    public void close(Player player) {
        HandlerList.unregisterAll(this);
    }

    public void click(Player player, int slot) {
        switch (slot) {
            case 45:
                player.closeInventory();
                new ActionsUI(spawner).open(player);
                break;
            case 48:
                if (page > 0) {
                    page--;
                    initializeItems();
                }
                break;
            case 49:
                if (player.getInventory().firstEmpty() == -1) {
                    for (Action action : SpawnerGenz.getInstance().getActions().get(ActionOpportunity.CLAIM_LOOT).get(ActionStatus.FAIL)) {
                        action.execute(player, new Placeholder[]{
                                new Placeholder("player", player.getName()),
                                new Placeholder("spawner_type", spawner.getCreatureSpawner().getSpawnedType().name())
                        });
                    }

                    initializeItems();
                    break;
                }

                for (int i = spawner.getStorage().size() - 1; i >= 0; i--) {
                    if (player.getInventory().firstEmpty() == -1) {
                        break;
                    }

                    player.getInventory().addItem(spawner.getStorage().get(i));
                    spawner.getStorage().remove(i);
                }

                for (Action action : SpawnerGenz.getInstance().getActions().get(ActionOpportunity.CLAIM_LOOT).get(ActionStatus.SUCCESS)) {
                    action.execute(player, new Placeholder[]{
                            new Placeholder("player", player.getName()),
                            new Placeholder("spawner_type", spawner.getCreatureSpawner().getSpawnedType().name())
                    });
                }

                initializeItems();
                break;
            case 50:
                if (page < maxPage) {
                    page++;
                    initializeItems();
                }
                break;
            case 53:
                if (Hooks.getShopHook() == null || Hooks.getVaultEconomy() == null) {
                    player.sendRichMessage("<red>An error as occurred. Please contact the server administrators");
                    return;
                }
                Double sellPrice = Hooks.getShopHook().getSellPrice(player, spawner.getStorage());
                Hooks.getVaultEconomy().depositPlayer(player, sellPrice);
                player.sendMessage(MiniMessage.miniMessage()
                        .deserialize(SpawnerGenz.getInstance().getConfig().getString("messages.sold-items"),
                                net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("sell_amount", String.valueOf(sellPrice))
                        ));
                spawner.getStorage().clear();
                initializeItems();
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
