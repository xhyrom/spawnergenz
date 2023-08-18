package me.xhyrom.spawnergenz.listeners;

import me.xhyrom.spawnergenz.SpawnerGenz;
import me.xhyrom.spawnergenz.structs.Spawner;
import me.xhyrom.spawnergenz.ui.ActionsUI;
import me.xhyrom.spawnergenz.utils.Utils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class ClickListener implements Listener {
    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.SPAWNER) return;
        if (event.getAction().name().contains("LEFT")) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        event.setCancelled(true);

        Player player = event.getPlayer();

        Spawner spawner = Spawner.fromCreatureSpawner((CreatureSpawner) event.getClickedBlock().getState(false));
        if (spawner.isReady()) {
            handle(player, spawner);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(SpawnerGenz.getInstance(), () -> {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    SpawnerGenz.getInstance().getConfig().getString("messages.loading-spawner")
            ));

            while (!spawner.isReady()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Bukkit.getScheduler().runTask(SpawnerGenz.getInstance(), () -> handle(player, spawner));
        });
    }

    private void handle(Player player, Spawner spawner) {
        if (player.getInventory().getItemInMainHand().getType() == Material.SPAWNER) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            ItemMeta itemInHandMeta = itemInHand.getItemMeta();
            PersistentDataContainer itemInHandPDC = itemInHandMeta.getPersistentDataContainer();

            NamespacedKey countKey = NamespacedKey.fromString("count", SpawnerGenz.getInstance());
            NamespacedKey mobKey = NamespacedKey.fromString("ms_mob", SpawnerGenz.getInstance());
            EntityType handMob = itemInHandPDC.has(mobKey) ? EntityType.valueOf(itemInHandPDC.get(mobKey, PersistentDataType.STRING)) : EntityType.PIG;

            int count = itemInHandPDC.getOrDefault(countKey, PersistentDataType.INTEGER, 1);

            if (
                    handMob != spawner.getCreatureSpawner().getSpawnedType()
            ) {
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        Objects.requireNonNull(SpawnerGenz.getInstance().getConfig().getString("messages.failed-to-merge-type"))
                ));
                return;
            }

            int maxStackSize = SpawnerGenz.getInstance().getConfig().getInt("spawners.max-stack-size");

            if (spawner.getCount() != maxStackSize) {
                if (count > 1) {
                    int allowToMerge = maxStackSize - spawner.getCount();
                    int actuallyMerged;

                    if (player.isSneaking()) {
                        if (allowToMerge > count) {
                            spawner.setCount(spawner.getCount() + count);

                            actuallyMerged = count;
                        } else {
                            spawner.setCount(maxStackSize);
                            actuallyMerged = allowToMerge;
                        }
                    } else {
                        spawner.setCount(spawner.getCount() + 1);

                        actuallyMerged = 1;
                    }

                    int amount = count - actuallyMerged;

                    if (amount == 0) {
                        if (itemInHand.getAmount() == 1) {
                            player.getInventory().setItemInMainHand(null);
                        } else {
                            itemInHand.setAmount(itemInHand.getAmount() - 1);
                        }
                    } else {
                        itemInHandPDC.set(countKey, PersistentDataType.INTEGER, amount);

                        itemInHandMeta.displayName(MiniMessage.miniMessage().deserialize(
                                SpawnerGenz.getInstance().getConfig().getString("item-spawner-name"),
                                Placeholder.parsed("amount", String.valueOf(amount)),
                                Placeholder.parsed("spawner_type", Utils.convertUpperSnakeCaseToPascalCase(
                                        spawner.getCreatureSpawner().getSpawnedType().name()
                                ))
                        ));

                        itemInHand.setItemMeta(itemInHandMeta);
                    }
                } else {
                    spawner.setCount(spawner.getCount() + 1);

                    if (itemInHand.getAmount() == 1) {
                        player.getInventory().setItemInMainHand(null);
                    } else itemInHand.setAmount(itemInHand.getAmount() - 1);
                }
            } else {
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        SpawnerGenz.getInstance().getConfig().getString("messages.failed-to-merge-max"),
                        Placeholder.parsed("spawner_stack_limit", String.valueOf(maxStackSize))
                ));

                return;
            }

            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    SpawnerGenz.getInstance().getConfig().getString("messages.success-merge"),
                    Placeholder.parsed("count", String.valueOf(spawner.getCount())),
                    Placeholder.parsed("spawner_type", Utils.convertUpperSnakeCaseToPascalCase(
                            spawner.getCreatureSpawner().getSpawnedType().name()
                    ))
            ));
            return;
        }

        new ActionsUI(spawner).open(player);
    }
}
