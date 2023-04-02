package me.xhyrom.spawnergenz.utils;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.LivingEntity;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class Utils {
    public static int getExpReward(LivingEntity entity) {
        try {
            Object handle = entity.getClass().getMethod("getHandle").invoke(entity);

            Method getExpReward = handle.getClass().getMethod("getExpReward");
            getExpReward.setAccessible(true);
            return (int) getExpReward.invoke(handle);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static String convertUpperSnakeCaseToPascalCase(String upperSnakeCase) {
        String[] parts = upperSnakeCase.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(part.substring(0, 1).toUpperCase());
            sb.append(part.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    public static String formatNumber(int number) {
        if (number < 1000) {
            return String.valueOf(number);
        }
        int exp = (int) (Math.log(number) / Math.log(1000));
        return String.format("%.1f%c", number / Math.pow(1000, exp), "kMGTPE".charAt(exp - 1));
    }

    public static TagResolver.Single[] transformPlaceholders(me.xhyrom.spawnergenz.structs.Placeholder[] placeholders) {
        ArrayList<TagResolver.Single> placeholderList = new ArrayList<>();

        for (me.xhyrom.spawnergenz.structs.Placeholder placeholder : placeholders) {
            placeholderList.add(Placeholder.parsed(placeholder.key(), placeholder.value()));
        }

        return placeholderList.toArray(new TagResolver.Single[0]);
    }
}
