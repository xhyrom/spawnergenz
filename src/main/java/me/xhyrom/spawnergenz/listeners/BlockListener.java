package me.xhyrom.spawnergenz.listeners;

import me.xhyrom.spawnergenz.SpawnerGenz;
import me.xhyrom.spawnergenz.structs.Spawner;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BlockListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.SPAWNER) return;
        event.setDropItems(false);
        event.setCancelled(true);

        Player player = event.getPlayer();
        if (!player.getInventory().getItemInMainHand().hasEnchant(Enchantment.SILK_TOUCH)) {
            SpawnerGenz.getInstance().getSpawners().remove(event.getBlock().getLocation());
            return;
        }

        Spawner spawner = Spawner.fromCreatureSpawner((CreatureSpawner) event.getBlock().getState(false));
        if (spawner.isReady()) {
            handleBreak(event, spawner);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(SpawnerGenz.getInstance(), () -> {
            while (!spawner.isReady()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Bukkit.getScheduler().runTask(SpawnerGenz.getInstance(), () -> handleBreak(event, spawner));
        });
    }

    private void handleBreak(BlockBreakEvent event, Spawner spawner) {
        Player player = event.getPlayer();

        if (player.isSneaking()) {
            ItemStack item = new ItemStack(Material.SPAWNER);
            BlockStateMeta itemMeta = (BlockStateMeta) item.getItemMeta();
            CreatureSpawner itemCreatureSpawner = (CreatureSpawner) itemMeta.getBlockState();

            itemCreatureSpawner.setSpawnedType(spawner.getCreatureSpawner().getSpawnedType());
            itemMeta.setBlockState(itemCreatureSpawner);
            itemMeta.getPersistentDataContainer().set(
                    NamespacedKey.fromString("count", SpawnerGenz.getInstance()),
                    PersistentDataType.INTEGER,
                    spawner.getCount()
            );
            itemMeta.displayName(
                    MiniMessage.miniMessage().deserialize(
                            "<yellow>" + spawner.getCount() + " <white>" + spawner.getCreatureSpawner().getSpawnedType().name() + " spawner"
                    )
            );
            item.setItemMeta(itemMeta);

            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);

            event.getBlock().setType(Material.AIR);
            SpawnerGenz.getInstance().getSpawners().remove(event.getBlock().getLocation());

            return;
        }

        ItemStack item = new ItemStack(Material.SPAWNER);
        BlockStateMeta itemMeta = (BlockStateMeta) item.getItemMeta();
        CreatureSpawner itemCreatureSpawner = (CreatureSpawner) itemMeta.getBlockState();

        itemCreatureSpawner.setSpawnedType(spawner.getCreatureSpawner().getSpawnedType());
        itemMeta.setBlockState(itemCreatureSpawner);
        item.setItemMeta(itemMeta);

        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);

        if (spawner.getCount() > 1) spawner.setCount(spawner.getCount() - 1);
        else {
            event.getBlock().setType(Material.AIR);
            SpawnerGenz.getInstance().getSpawners().remove(event.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() != Material.SPAWNER) return;
        ItemStack item = event.getItemInHand();

        Player player = event.getPlayer();
        Spawner spawner = Spawner.fromCreatureSpawner((CreatureSpawner) event.getBlock().getState(false));
        if (spawner.isReady()) {
            handlePlace(event, item, spawner);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(SpawnerGenz.getInstance(), () -> {
            while (!spawner.isReady()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Bukkit.getScheduler().runTask(SpawnerGenz.getInstance(), () -> handlePlace(event, item, spawner));
        });
    }

    private void handlePlace(BlockPlaceEvent event, ItemStack item, Spawner spawner) {
        if (!item.hasItemMeta()) return;

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = NamespacedKey.fromString("count", SpawnerGenz.getInstance());

        System.out.println(container);
        if (!container.has(key)) return;

        int count = container.get(key, PersistentDataType.INTEGER);
        spawner.setCount(count);

        container.remove(key);
    }
}
