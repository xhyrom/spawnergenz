package me.xhyrom.spawnergenz.structs.actions;

import me.xhyrom.spawnergenz.structs.Placeholder;
import org.bukkit.entity.Player;

public interface Action {
    void execute(Player player, Placeholder[] placeholders);
}
