package me.xhyrom.spawnergenz.structs;

import me.xhyrom.spawnergenz.SpawnerGenz;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Collection;
import java.util.HashMap;

public class TTLHashMap {
    private HashMap<Location, Spawner> map = new HashMap<>();
    private HashMap<Location, Long> ttl = new HashMap<>();
    private long ttlTime;

    public TTLHashMap(long ttlTime) {
        this.ttlTime = ttlTime;

        Bukkit.getScheduler().runTaskTimerAsynchronously(SpawnerGenz.getInstance(), () -> {
            for (Location key : map.keySet()) {
                get(key).saveToPDC();

                if (ttl.get(key) < System.currentTimeMillis()) {
                    remove(key);
                }
            }
        }, 0, 60 * 20L);
    }

    public void put(Location key, Spawner value) {
        map.put(key, value);
        ttl.put(key, System.currentTimeMillis() + ttlTime);
    }

    public Spawner get(Location key) {
        return map.get(key);
    }

    public Collection<Spawner> values() {
        return map.values();
    }

    public void remove(Location key) {
        Bukkit.getScheduler().runTaskAsynchronously(SpawnerGenz.getInstance(), () -> {
            if (map.get(key) != null) map.get(key).saveToPDC();

            map.remove(key);
            ttl.remove(key);
        });
    }
}
