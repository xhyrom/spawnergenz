package me.xhyrom.spawnergenz.hooks.shop;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.xhyrom.spawnergenz.SpawnerGenz;
import me.xhyrom.spawnergenz.hooks.ShopHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public class EconomyShopGUI implements ShopHook {
    public EconomyShopGUI() {
        Plugin economyShopGUIPlugin = Bukkit.getPluginManager().getPlugin("EconomyShopGUI");
        if (economyShopGUIPlugin == null || !economyShopGUIPlugin.isEnabled()) {
            Plugin economyShopGUIPremiumPlugin = Bukkit.getPluginManager().getPlugin("EconomyShopGUI-Premium");
            if (economyShopGUIPremiumPlugin == null || !economyShopGUIPremiumPlugin.isEnabled()) {
                SpawnerGenz.getInstance().getLogger().severe("EconomyShopGUI is set as the shop but is either not installed or is disabled. Disabling SpawnerGenz...");
                Bukkit.getPluginManager().disablePlugin(SpawnerGenz.getInstance());
            }
        }
    }
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
