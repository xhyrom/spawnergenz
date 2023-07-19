package me.xhyrom.spawnergenz.hooking;

import lombok.Getter;
import me.xhyrom.spawnergenz.SpawnerGenz;
import me.xhyrom.spawnergenz.hooking.hooks.shop.EconomyShopGUI;
import me.xhyrom.spawnergenz.hooking.hooks.shop.EssentialsShop;
import me.xhyrom.spawnergenz.hooking.hooks.shop.PeddlersPocket;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import static org.bukkit.Bukkit.getServer;

public class Hooks {
    @Getter
    private static Economy vaultEconomy = null;

    @Getter
    private static ShopHook shopHook = null;
    public static void init() {
        loadVault();
        loadShops();
    }
    private static void loadVault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            SpawnerGenz.getInstance().getLogger().severe("Failed to hook into Vault. Without Vault, SpawnerGenz may not function correctly!");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        vaultEconomy = rsp.getProvider();
    }
    private static void loadShops() {
        String configuredShopPlugin = SpawnerGenz.getInstance().getConfig().getString("shop-plugin");
        if (configuredShopPlugin.equals("UNCONFIGURED")) {
            SpawnerGenz.getInstance().getLogger().severe("No shop plugin has been configured. Player's will not be able to sell the loot from spawners without it!");
            return;
        }
        switch (configuredShopPlugin) {
            case "Essentials":
                shopHook = new EssentialsShop();
                break;
            case "EconomyShopGUI":
            case "EconomyShopGUI-Premium":
                shopHook = new EconomyShopGUI();
                break;
            case "PeddlersPocket":
                shopHook = new PeddlersPocket();
                break;
            default:
                SpawnerGenz.getInstance().getLogger().severe("SpawnerGenz does not currently support "+configuredShopPlugin+" as a valid shop plugin!");
                break;
        }
    }
}
