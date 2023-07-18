package me.xhyrom.spawnergenz.hooking.hooks.shop;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.xhyrom.spawnergenz.hooking.ShopHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class EconomyShopGUI implements ShopHook {
    @Override
    @SuppressWarnings("DEPRECATED")
    public Double getSellPrice(Player player, ArrayList<ItemStack> itemStacks) {
        Double totalPrice = 0.0;
        for (ItemStack itemStack : itemStacks) {
            Double price = EconomyShopGUIHook.getItemSellPrice(player, itemStack);
            if (price == null || price.isNaN()) {
                return 0.00;
            }
            totalPrice += price;
        }
        return totalPrice;
    }
}
