package me.xhyrom.spawnergenz.structs.actions;

import me.xhyrom.spawnergenz.structs.Placeholder;
import me.xhyrom.spawnergenz.utils.Utils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

public class TitleAction implements Action {
    private String title;
    private String subtitle = "";

    public TitleAction(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    @Override
    public void execute(Player player, Placeholder[] placeholders) {
        player.showTitle(Title.title(
                MiniMessage.miniMessage().deserialize(
                        title,
                        Utils.transformPlaceholders(placeholders)
                ),
                MiniMessage.miniMessage().deserialize(
                        subtitle,
                        Utils.transformPlaceholders(placeholders)
                ))
        );
    }
}