package me.xhyrom.spawnergenz.structs;

import com.jeff_media.morepersistentdatatypes.DataType;
import lombok.Getter;
import lombok.Setter;
import me.xhyrom.spawnergenz.SpawnerGenz;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;

public class Spawner {
    @Getter
    private CreatureSpawner creatureSpawner;
    @Getter
    @Setter
    private int count;
    @Getter
    @Setter
    private int experience;
    @Getter
    @Setter
    private ArrayList<ItemStack> storage;
    @Getter
    @Setter
    private boolean ready;

    public Spawner(CreatureSpawner creatureSpawner) {
        this.creatureSpawner = creatureSpawner;

        this.ready = false;
        this.count = 0;
        this.experience = 0;
        this.storage = new ArrayList<>();

        Bukkit.getScheduler().runTaskAsynchronously(SpawnerGenz.getInstance(), () -> {
            this.ready = true;
            this.count = getCountFromPDC();
            this.experience = getExperienceFromPDC();
            this.storage = new ArrayList<>(Arrays.stream(getStorageFromPDC()).toList());
        });

        SpawnerGenz.getInstance().getSpawners().put(creatureSpawner.getLocation(), this);
    }

    public static Spawner fromCreatureSpawner(CreatureSpawner creatureSpawner) {
        Spawner spawner = SpawnerGenz.getInstance().getSpawners().get(creatureSpawner.getLocation());

        return spawner != null ? spawner : new Spawner(creatureSpawner);
    }

    public void saveToPDC() {
        setCountToPDC(count);
        setExperienceToPDC(experience);
        setStorageToPDC(storage.toArray(new ItemStack[0]));
    }

    public void addItemToStorage(ItemStack item) {
        ItemStack[] storage = this.storage.toArray(new ItemStack[0]);

        // Check if the item can be added to an existing stack
        for (ItemStack stack : storage) {
            if (stack.isSimilar(item) && stack.getAmount() < stack.getMaxStackSize()) {
                int remainingSpace = stack.getMaxStackSize() - stack.getAmount();
                if (remainingSpace >= item.getAmount()) {
                    // Add the entire item stack to the existing stack
                    stack.setAmount(stack.getAmount() + item.getAmount());
                    this.setStorage(new ArrayList<>(Arrays.stream(storage).toList()));
                    return;
                } else {
                    // Add as much as possible to the existing stack, and continue with the remaining items
                    stack.setAmount(stack.getMaxStackSize());
                    item.setAmount(item.getAmount() - remainingSpace);
                }
            }
        }

        // If we reach this point, the item couldn't be added to an existing stack, so create a new stack
        while (item.getAmount() > 0) {
            ItemStack newStack = item.clone();
            if (item.getAmount() <= newStack.getMaxStackSize()) {
                newStack.setAmount(item.getAmount());
                item.setAmount(0);
            } else {
                newStack.setAmount(newStack.getMaxStackSize());
                item.setAmount(item.getAmount() - newStack.getMaxStackSize());
            }
            storage = Arrays.copyOf(storage, storage.length + 1);
            storage[storage.length - 1] = newStack;
        }

        this.setStorage(new ArrayList<>(Arrays.stream(storage).toList()));;
    }

    private void setCountToPDC(int count) {
        creatureSpawner.getPersistentDataContainer()
                .set(
                        NamespacedKey.fromString("count", SpawnerGenz.getInstance()),
                        PersistentDataType.INTEGER,
                        count
                );

        Bukkit.getScheduler().runTask(SpawnerGenz.getInstance(), () -> creatureSpawner.update());
    }
    private int getCountFromPDC() {
        return creatureSpawner.getPersistentDataContainer().getOrDefault(
                NamespacedKey.fromString("count", SpawnerGenz.getInstance()),
                PersistentDataType.INTEGER,
                1
        );
    }
    private void setExperienceToPDC(int xps) {
        creatureSpawner.getPersistentDataContainer()
                .set(
                        NamespacedKey.fromString("experience", SpawnerGenz.getInstance()),
                        PersistentDataType.INTEGER,
                        xps
                );

        Bukkit.getScheduler().runTask(SpawnerGenz.getInstance(), () -> creatureSpawner.update());
    }
    private int getExperienceFromPDC() {
        return creatureSpawner.getPersistentDataContainer().getOrDefault(
                NamespacedKey.fromString("experience", SpawnerGenz.getInstance()),
                PersistentDataType.INTEGER,
                0
        );
    }
    private void setStorageToPDC(ItemStack[] items) {
        creatureSpawner.getPersistentDataContainer()
                .set(
                        NamespacedKey.fromString("storage", SpawnerGenz.getInstance()),
                        DataType.ITEM_STACK_ARRAY,
                        items
                );

        Bukkit.getScheduler().runTask(SpawnerGenz.getInstance(), () -> creatureSpawner.update());
    }
    private ItemStack[] getStorageFromPDC() {
        return creatureSpawner.getPersistentDataContainer().getOrDefault(
                NamespacedKey.fromString("storage", SpawnerGenz.getInstance()),
                DataType.ITEM_STACK_ARRAY,
                new ItemStack[0]
        );
    }
}
