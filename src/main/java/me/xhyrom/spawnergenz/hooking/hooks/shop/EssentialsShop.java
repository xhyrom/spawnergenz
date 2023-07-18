package me.xhyrom.spawnergenz.hooking.hooks.shop;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Worth;
import me.xhyrom.spawnergenz.SpawnerGenz;
import me.xhyrom.spawnergenz.hooking.ShopHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public class EssentialsShop implements ShopHook {
    private Essentials essentials;
    private Worth worth;

    public EssentialsShop() {
        Plugin essentialsXPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentialsXPlugin != null && essentialsXPlugin.isEnabled()) {
            this.essentials = (Essentials) essentialsXPlugin;
            this.worth = essentials.getWorth();
        } else {
            SpawnerGenz.getInstance().getLogger().severe("EssentialsX is set as the shop but is either not installed or is disabled. Disabling SpawnerGenz...");
            Bukkit.getPluginManager().disablePlugin(SpawnerGenz.getInstance());
        }
    }
    @Override
    public Double getSellPrice(Player player, ArrayList<ItemStack> itemStacks) {
        Double totalPrice = 0.0;
        for (ItemStack itemStack : itemStacks) {
            Double price = this.worth.getPrice(this.essentials, itemStack).doubleValue();
            if (price == null || price.isNaN()) {
                return 0.00;
            }
            totalPrice += price * itemStack.getAmount();
        }
        return totalPrice;
    }
}
