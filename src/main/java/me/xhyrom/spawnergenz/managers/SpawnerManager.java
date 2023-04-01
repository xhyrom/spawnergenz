package me.xhyrom.spawnergenz.managers;

import com.jeff_media.morepersistentdatatypes.DataType;
import me.xhyrom.spawnergenz.SpawnerGenz;
import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpawnerManager {
    public static void addSpawnerCount(CreatureSpawner spawner) {
        spawner.getPersistentDataContainer()
                .set(
                        NamespacedKey.fromString("count", SpawnerGenz.getInstance()),
                        PersistentDataType.INTEGER,
                        getSpawnerCount(spawner) + 1
                );

        spawner.update();
    }

    public static int getSpawnerCount(CreatureSpawner spawner) {
        return spawner.getPersistentDataContainer().getOrDefault(
                NamespacedKey.fromString("count", SpawnerGenz.getInstance()),
                PersistentDataType.INTEGER,
                1
        );
    }

    public static void addStemToSpawnerStorage(CreatureSpawner spawner, ItemStack item) {
        ItemStack[] storage = getSpawnerStorage(spawner);

        // Check if the item can be added to an existing stack
        for (int i = 0; i < storage.length; i++) {
            ItemStack stack = storage[i];
            if (stack.isSimilar(item) && stack.getAmount() < stack.getMaxStackSize()) {
                int remainingSpace = stack.getMaxStackSize() - stack.getAmount();
                if (remainingSpace >= item.getAmount()) {
                    // Add the entire item stack to the existing stack
                    stack.setAmount(stack.getAmount() + item.getAmount());
                    setSpawnerStorage(spawner, storage);
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

        setSpawnerStorage(spawner, storage);
    }

    public static void setSpawnerStorage(CreatureSpawner spawner, ItemStack[] items) {
        spawner.getPersistentDataContainer()
                .set(
                        NamespacedKey.fromString("storage", SpawnerGenz.getInstance()),
                        DataType.ITEM_STACK_ARRAY,
                        items
                );

        spawner.update();
    }

    public static ItemStack[] getSpawnerStorage(CreatureSpawner spawner) {
        return spawner.getPersistentDataContainer().getOrDefault(
                NamespacedKey.fromString("storage", SpawnerGenz.getInstance()),
                DataType.ITEM_STACK_ARRAY,
                new ItemStack[0]
        );
    }
}
