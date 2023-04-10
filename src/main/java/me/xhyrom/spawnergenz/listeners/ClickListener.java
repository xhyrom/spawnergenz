package me.xhyrom.spawnergenz.listeners;

import me.xhyrom.spawnergenz.SpawnerGenz;
import me.xhyrom.spawnergenz.structs.Spawner;
import me.xhyrom.spawnergenz.ui.ActionsUI;
import me.xhyrom.spawnergenz.utils.Utils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

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
            CreatureSpawner handSpawner = (CreatureSpawner) ((BlockStateMeta) itemInHand.getItemMeta()).getBlockState();

            if (handSpawner.getSpawnedType() != spawner.getCreatureSpawner().getSpawnedType()) {
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        SpawnerGenz.getInstance().getConfig().getString("messages.failed-to-merge-type")
                ));
                return;
            }

            if (player.isSneaking()) {
                int remove = 0;
                for (int i = 0; i < itemInHand.getAmount(); i++) {
                    if (spawner.getCount() == 256) {
                        if (i == 0)
                            player.sendMessage(MiniMessage.miniMessage().deserialize(
                                    SpawnerGenz.getInstance().getConfig().getString("messages.failed-to-merge-max")
                            ));

                        if ((itemInHand.getAmount() - remove) <= 0) {
                            player.getInventory().setItemInMainHand(null);
                        } else itemInHand.setAmount(itemInHand.getAmount() - remove);

                        return;
                    }

                    spawner.setCount(spawner.getCount() + 1);
                    remove++;
                }

                if ((itemInHand.getAmount() - remove) <= 0) {
                    player.getInventory().setItemInMainHand(null);
                } else itemInHand.setAmount(itemInHand.getAmount() - remove);
            } else {
                if (spawner.getCount() != 256) {
                    spawner.setCount(spawner.getCount() + 1);

                    if (itemInHand.getAmount() == 1) {
                        player.getInventory().setItemInMainHand(null);
                    } else itemInHand.setAmount(itemInHand.getAmount() - 1);
                } else {
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            SpawnerGenz.getInstance().getConfig().getString("messages.failed-to-merge-max")
                    ));

                    return;
                }
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
