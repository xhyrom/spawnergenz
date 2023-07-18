package me.xhyrom.spawnergenz.hooking.hooks.shop;

import dev.xhyrom.peddlerspocket.api.PeddlersPocketAPI;
import me.xhyrom.spawnergenz.hooking.ShopHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PeddlersPocket implements ShopHook {
    @Override
    public Double getSellPrice(Player player, ArrayList<ItemStack> itemStacks) {
        Double totalPrice = 0.0;
        for (ItemStack itemStack : itemStacks) {
            Double price = PeddlersPocketAPI.getPrice(itemStack.getType());
            if (price == null || price.isNaN()) {
                return 0.00;
            }
            totalPrice += price * itemStack.getAmount();
        }
        return totalPrice;
    }
}
