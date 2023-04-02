package me.xhyrom.spawnergenz.listeners;

import me.xhyrom.spawnergenz.SpawnerGenz;
import me.xhyrom.spawnergenz.structs.Spawner;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

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
            handle(event, spawner);
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

            Bukkit.getScheduler().runTask(SpawnerGenz.getInstance(), () -> handle(event, spawner));
        });
    }

    private void handle(BlockBreakEvent event, Spawner spawner) {
        Player player = event.getPlayer();

        if (player.isSneaking()) {
            for (int i = 0; i < spawner.getCount(); i++) {
                ItemStack item = new ItemStack(Material.SPAWNER);
                BlockStateMeta itemMeta = (BlockStateMeta) item.getItemMeta();
                CreatureSpawner itemCreatureSpawner = (CreatureSpawner) itemMeta.getBlockState();

                itemCreatureSpawner.setSpawnedType(spawner.getCreatureSpawner().getSpawnedType());
                itemMeta.setBlockState(itemCreatureSpawner);
                item.setItemMeta(itemMeta);

                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
            }

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
}
