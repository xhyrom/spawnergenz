package me.xhyrom.spawnergenz.structs.actions;

import me.xhyrom.spawnergenz.structs.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class CommandAction implements Action {
    private String command;

    public CommandAction(String command) {
        this.command = command;
    }

    @Override
    public void execute(Player player, Placeholder[] placeholders) {
        String commandCopy = this.command;
        for (Placeholder placeholder : placeholders) {
            commandCopy = commandCopy.replace(placeholder.key(), placeholder.value());
        }

        Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                commandCopy
        );
    }
}
