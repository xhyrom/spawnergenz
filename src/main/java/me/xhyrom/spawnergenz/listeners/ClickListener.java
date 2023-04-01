package me.xhyrom.spawnergenz.listeners;

import me.xhyrom.spawnergenz.SpawnerGenz;
import me.xhyrom.spawnergenz.managers.SpawnerManager;
import me.xhyrom.spawnergenz.ui.ActionsUI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ClickListener implements Listener {
    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.SPAWNER) return;
        if (event.getAction().name().contains("LEFT")) return;

        Player player = event.getPlayer();
        event.setCancelled(true);

        CreatureSpawner spawner = (CreatureSpawner) event.getClickedBlock().getState(false);

        if (player.getInventory().getItemInMainHand().getType() == Material.SPAWNER) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            CreatureSpawner handSpawner = (CreatureSpawner) ((BlockStateMeta) itemInHand.getItemMeta()).getBlockState();

            if (handSpawner.getSpawnedType() != spawner.getSpawnedType()) {
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        "<red>You can only merge spawners with the same type!"
                ));
                return;
            }

            if (player.isSneaking()) {
                for (int i = 0; i < itemInHand.getAmount(); i++) {
                    SpawnerManager.addSpawnerCount(spawner);
                }

                player.getInventory().setItemInMainHand(null);
            } else {
                SpawnerManager.addSpawnerCount(spawner);

                if (itemInHand.getAmount() == 1) {
                    player.getInventory().setItemInMainHand(null);
                } else itemInHand.setAmount(itemInHand.getAmount() - 1);
            }

            event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(
                    "<green>You have merged <yellow>" + handSpawner.getSpawnedType().name().substring(0, 1) + handSpawner.getSpawnedType().name().substring(1).toLowerCase() + "<green> spawner with <yellow>" + spawner.getSpawnedType().name().substring(0, 1) + spawner.getSpawnedType().name().substring(1).toLowerCase() + "<green> spawner!"
            ));
            return;
        }

        new ActionsUI(spawner).open(event.getPlayer());
    }
}
