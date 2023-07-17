package me.xhyrom.spawnergenz;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import lombok.Getter;
import me.xhyrom.spawnergenz.commands.SpawnerGenzCommand;
import me.xhyrom.spawnergenz.hooks.ShopHook;
import me.xhyrom.spawnergenz.hooks.shop.EconomyShopGUI;
import me.xhyrom.spawnergenz.hooks.shop.EssentialsShop;
import me.xhyrom.spawnergenz.listeners.BlockListener;
import me.xhyrom.spawnergenz.listeners.ClickListener;
import me.xhyrom.spawnergenz.listeners.SpawnerListener;
import me.xhyrom.spawnergenz.structs.Spawner;
import me.xhyrom.spawnergenz.structs.TTLHashMap;
import me.xhyrom.spawnergenz.structs.actions.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

public class SpawnerGenz extends JavaPlugin {
    @Getter
    private static SpawnerGenz instance;
    @Getter
    private TTLHashMap spawners;
    @Getter
    private HashMap<ActionOpportunity, HashMap<ActionStatus, ArrayList<Action>>> actions = new HashMap<>();

    private HashMap<String, Class<? extends ShopHook>> shopPlugins = new HashMap<>();

    @Getter
    private Economy vaultEconomy;

    @Getter
    private ShopHook shopHook;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();
        saveDefaultConfig();
        if (!setupVault()) {
            this.getLogger().severe("Failed to hook into Vault!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!loadShops()) {
            this.getLogger().severe("Failed to hook into shop!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getConfig().getConfigurationSection("actions").getKeys(false).forEach(key -> {
            ActionOpportunity opportunity = ActionOpportunity.valueOf(key.toUpperCase());
            HashMap<ActionStatus, ArrayList<Action>> map = new HashMap<>();

            getConfig().getConfigurationSection("actions." + key).getKeys(false).forEach(key1 -> {
                ActionStatus status = ActionStatus.valueOf(key1.toUpperCase());
                ArrayList<Action> actions = new ArrayList<>();

                ((ArrayList<HashMap<String, Object>>) getConfig().get("actions." + key + "." + key1)).forEach(action -> {
                    if (action.containsKey("title"))
                        actions.add(new TitleAction(
                                action.get("title").toString(),
                                action.containsKey("subtitle") ? action.get("subtitle").toString() : ""
                        ));
                    else if (action.containsKey("message"))
                        actions.add(new MessageAction(
                                action.get("message").toString(),
                                action.containsKey("broadcast") && (boolean) action.get("broadcast")
                        ));
                    else if (action.containsKey("command"))
                        actions.add(new CommandAction(
                                action.get("command").toString()
                        ));
                    else if (action.containsKey("sound"))
                        actions.add(new SoundAction(
                                action.get("sound").toString(),
                                action.containsKey("volume") ? Float.parseFloat(action.get("volume").toString()) : 1,
                                action.containsKey("pitch") ? Float.parseFloat(action.get("pitch").toString()) : 1
                        ));
                });

                map.put(status, actions);
            });

            this.actions.put(opportunity, map);
        });

        instance = this;
        spawners = new TTLHashMap(300000);

        getServer().getPluginManager().registerEvents(new ClickListener(), this);
        getServer().getPluginManager().registerEvents(new SpawnerListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        SpawnerGenzCommand.register();
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
        for (Spawner spawner : spawners.values()) {
            spawner.saveToPDCSync();
        }
    }

    private boolean setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        this.vaultEconomy = rsp.getProvider();
        return this.vaultEconomy != null;
    }
    private boolean loadShops() {
        // This could probably be improved
        shopPlugins.put("Essentials", EssentialsShop.class);
        shopPlugins.put("EconomyShopGUI", EconomyShopGUI.class);
        shopPlugins.put("EconomyShopGUI-Premium", EconomyShopGUI.class);
        shopPlugins.put("PeddlersPocket", EconomyShopGUI.class);
        Class<? extends ShopHook> pluginClass = shopPlugins.get(this.getConfig().getString("shop-plugin"));
        if (pluginClass != null) {
            try {
                this.shopHook = pluginClass.getConstructor().newInstance();
                return true;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                this.getLogger().severe(e.getMessage());
                Bukkit.getPluginManager().disablePlugin(this);
                return false;
            }
        } else {
            this.getLogger().severe("Unfortunately SpawnerGenz does not support "+this.getConfig().getString("shop-plugin")+" at this time :(");
            return false;
        }
    }
}