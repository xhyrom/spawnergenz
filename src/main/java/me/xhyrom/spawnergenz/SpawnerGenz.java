package me.xhyrom.spawnergenz;

import lombok.Getter;
import me.xhyrom.spawnergenz.listeners.ClickListener;
import me.xhyrom.spawnergenz.listeners.SpawnerListener;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnerGenz extends JavaPlugin {
    @Getter
    private static SpawnerGenz instance;

    @Override
    public void onEnable() {
        instance = this;

        getServer().getPluginManager().registerEvents(new ClickListener(), this);
        getServer().getPluginManager().registerEvents(new SpawnerListener(), this);
    }
}