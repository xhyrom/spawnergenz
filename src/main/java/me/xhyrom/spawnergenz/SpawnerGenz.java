package me.xhyrom.spawnergenz;

import lombok.Getter;
import me.xhyrom.spawnergenz.listeners.BlockListener;
import me.xhyrom.spawnergenz.listeners.ClickListener;
import me.xhyrom.spawnergenz.listeners.SpawnerListener;
import me.xhyrom.spawnergenz.structs.Spawner;
import me.xhyrom.spawnergenz.structs.TTLHashMap;
import me.xhyrom.spawnergenz.structs.actions.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

public class SpawnerGenz extends JavaPlugin {
    @Getter
    private static SpawnerGenz instance;
    @Getter
    private TTLHashMap spawners;
    @Getter
    private HashMap<ActionOpportunity, HashMap<ActionStatus, ArrayList<Action>>> actions = new HashMap<>();
    public FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        config.getConfigurationSection("actions").getKeys(false).forEach(key -> {
            ActionOpportunity opportunity = ActionOpportunity.valueOf(key.toUpperCase());
            HashMap<ActionStatus, ArrayList<Action>> map = new HashMap<>();

            config.getConfigurationSection("actions." + key).getKeys(false).forEach(key1 -> {
                ActionStatus status = ActionStatus.valueOf(key1.toUpperCase());
                ArrayList<Action> actions = new ArrayList<>();

                ((ArrayList<HashMap<String, Object>>) config.get("actions." + key + "." + key1)).forEach(action -> {
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

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (Spawner spawner : spawners.values()) {
                spawner.saveToPDC();
            }
        }, 0, 90 * 20L);
    }

    @Override
    public void onDisable() {
        for (Spawner spawner : spawners.values()) {
            spawner.saveToPDC();
        }
    }
}