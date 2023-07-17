package me.xhyrom.spawnergenz.hooks.shop;

import me.xhyrom.spawnergenz.hooks.ShopHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PeddlersPocket implements ShopHook {
    @Override
    public Double getSellPrice(Player player, ArrayList<ItemStack> itemStacks) {
        // TODO
        return 0.00;
    }
}
