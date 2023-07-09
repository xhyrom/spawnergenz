package me.xhyrom.spawnergenz.listeners;

import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import me.xhyrom.spawnergenz.SpawnerGenz;
import me.xhyrom.spawnergenz.structs.Spawner;
import me.xhyrom.spawnergenz.utils.Utils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BlockListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.SPAWNER) return;
        event.setDropItems(false);
        event.setCancelled(true);

        // Allow only silk touch 10+ (special pickaxe)
        // TODO: use oraxen api
        if (event.getPlayer().getInventory().getItemInMainHand().getEnchantLevel(Enchantment.SILK_TOUCH) != 10)
            return;

        event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));

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

            itemMeta.getPersistentDataContainer().set(
                    NamespacedKey.fromString("ms_mob", SpawnerGenz.getInstance()),
                    PersistentDataType.STRING,
                    spawner.getCreatureSpawner().getSpawnedType().name()
            );
            itemMeta.getPersistentDataContainer().set(
                    NamespacedKey.fromString("count", SpawnerGenz.getInstance()),
                    PersistentDataType.INTEGER,
                    spawner.getCount()
            );
            itemMeta.displayName(
                    MiniMessage.miniMessage().deserialize(
                            SpawnerGenz.getInstance().getConfig().getString("item-spawner-name"),
                            Placeholder.parsed("amount", String.valueOf(spawner.getCount())),
                            Placeholder.parsed("spawner_type", Utils.convertUpperSnakeCaseToPascalCase(spawner.getCreatureSpawner().getSpawnedType().name()))
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

        itemMeta.displayName(
                MiniMessage.miniMessage().deserialize(
                        SpawnerGenz.getInstance().getConfig().getString("item-spawner-name"),
                        Placeholder.parsed("amount", "1"),
                        Placeholder.parsed("spawner_type", Utils.convertUpperSnakeCaseToPascalCase(spawner.getCreatureSpawner().getSpawnedType().name()))
                )
        );
        itemMeta.getPersistentDataContainer().set(
                NamespacedKey.fromString("ms_mob", SpawnerGenz.getInstance()),
                PersistentDataType.STRING,
                spawner.getCreatureSpawner().getSpawnedType().name()
        );

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

        if (!item.hasItemMeta()) return;

        CreatureSpawner creatureSpawner = (CreatureSpawner) event.getBlock().getState(false);

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = NamespacedKey.fromString("count", SpawnerGenz.getInstance());
        NamespacedKey mobKey = NamespacedKey.fromString("ms_mob", SpawnerGenz.getInstance());
        EntityType spawnedType = container.has(mobKey, PersistentDataType.STRING) ?
                EntityType.valueOf(container.get(mobKey, PersistentDataType.STRING)) :
                EntityType.PIG;

        new NBTTileEntity(creatureSpawner).mergeCompound(
                new NBTContainer(
                        "{\"SpawnData\": {\"entity\": {\"id\": \"minecraft:"+spawnedType.name().toLowerCase()+"\"}, \"custom_spawn_rules\": {}}, \"MaxNearbyEntities\": \""+creatureSpawner.getMaxNearbyEntities()+"s\", \"MinSpawnDelay\": \""+creatureSpawner.getMinSpawnDelay()+"s\", \"y\": "+creatureSpawner.getY()+", \"id\": \"minecraft:mob_spawner\", \"SpawnPotentials\": [{\"weight\": 1, \"data\": {\"entity\": {\"id\": \"minecraft:"+spawnedType.name().toLowerCase()+"\"}, \"custom_spawn_rules\": {}}}], \"x\": "+creatureSpawner.getX()+", \"SpawnRange\": \""+creatureSpawner.getSpawnRange()+"s\", \"MaxSpawnDelay\": \""+creatureSpawner.getMaxSpawnDelay()+"s\", \"RequiredPlayerRange\": \""+creatureSpawner.getRequiredPlayerRange()+"s\", \"SpawnCount\": \""+creatureSpawner.getSpawnCount()+"s\", \"z\": "+creatureSpawner.getZ()+", \"Delay\": \""+creatureSpawner.getDelay()+"s\"}"
                )
        );

        Spawner spawner = Spawner.fromCreatureSpawner(creatureSpawner);
        if (spawner.isReady()) {
            handlePlace(key, mobKey, spawnedType, container, spawner);
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

            Bukkit.getScheduler().runTask(SpawnerGenz.getInstance(), () -> handlePlace(key, mobKey, spawnedType, container, spawner));
        });
    }

    private void handlePlace(NamespacedKey key, NamespacedKey mobKey, EntityType spawnedType, PersistentDataContainer container, Spawner spawner) {
        if (container.has(key)) {
            int count = container.get(key, PersistentDataType.INTEGER);
            spawner.setCount(count);

            container.remove(key);
        }

        if (container.has(mobKey)) {
            spawner.getCreatureSpawner().setSpawnedType(spawnedType);
            spawner.getCreatureSpawner().update();

            container.remove(mobKey);
        }
    }
}
