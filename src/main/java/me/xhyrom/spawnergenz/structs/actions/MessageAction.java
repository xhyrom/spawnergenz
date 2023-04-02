package me.xhyrom.spawnergenz.structs.actions;

import me.xhyrom.spawnergenz.structs.Placeholder;
import me.xhyrom.spawnergenz.utils.Utils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class MessageAction implements Action {
    private String message;
    private boolean broadcast;

    public MessageAction(String message, boolean broadcast) {
        this.message = message;
        this.broadcast = broadcast;
    }

    @Override
    public void execute(Player player, Placeholder[] placeholders) {
        if (broadcast) {
            Bukkit.broadcast(MiniMessage.miniMessage().deserialize(
                    this.message,
                    Utils.transformPlaceholders(placeholders)
            ));

            return;
        }

        player.sendMessage(MiniMessage.miniMessage().deserialize(
                this.message,
                Utils.transformPlaceholders(placeholders)
        ));
    }
}