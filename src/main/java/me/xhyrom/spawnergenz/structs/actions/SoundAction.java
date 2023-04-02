package me.xhyrom.spawnergenz.structs.actions;

import me.xhyrom.spawnergenz.structs.Placeholder;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class SoundAction implements Action {
    private String sound;
    private float volume = 1;
    private float pitch = 1;

    public SoundAction(String sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void execute(Player player, Placeholder[] placeholders) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
