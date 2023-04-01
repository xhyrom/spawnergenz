package me.xhyrom.spawnergenz.listeners;

import me.xhyrom.spawnergenz.managers.SpawnerManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;

import java.util.Random;

public class SpawnerListener implements Listener {
    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        event.setCancelled(true);

        LootTable lootTable = ((Lootable) event.getEntity()).getLootTable();

        for (int i = 0; i < SpawnerManager.getSpawnerCount(event.getSpawner()); i++) {
            LootContext lootContext = new LootContext.Builder(event.getLocation())
                    .lootedEntity(event.getEntity())
                    .build();

            for (ItemStack item : lootTable.populateLoot(new Random(), lootContext)) {
                SpawnerManager.addStemToSpawnerStorage(event.getSpawner(), item);
            }
        }
    }
}
