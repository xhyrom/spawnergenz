package me.xhyrom.spawnergenz.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntityTypeArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import me.xhyrom.spawnergenz.SpawnerGenz;
import me.xhyrom.spawnergenz.utils.Utils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataType;

public class SpawnerGenzCommand {
    public static void register() {
        new CommandAPICommand("spawnergenz")
                .withPermission("spawnergenz.admin")
                .withSubcommand(new CommandAPICommand("give")
                        .withArguments(new EntityTypeArgument("entity"), new IntegerArgument("amount"))
                        .executesPlayer(SpawnerGenzCommand::give)
                )
                .withSubcommand(new CommandAPICommand("give")
                        .withArguments(new EntityTypeArgument("entity"), new IntegerArgument("amount"), new PlayerArgument("player"))
                        .executes(SpawnerGenzCommand::give)
                )
                .withSubcommand(new CommandAPICommand("give")
                        .withArguments(new EntityTypeArgument("entity"))
                        .executesPlayer(SpawnerGenzCommand::give)
                )
                .register();
    }

    public static void give(CommandSender sender, CommandArguments args) {
        EntityType entityType = (EntityType) args.get("entity");
        int amount = (int) args.getOptional("amount").orElse(1);
        Player target = (Player) args.getOptional("player").orElse(sender);

        ItemStack item = new ItemStack(Material.SPAWNER);
        BlockStateMeta itemMeta = (BlockStateMeta) item.getItemMeta();

        itemMeta.displayName(
                MiniMessage.miniMessage().deserialize(
                        SpawnerGenz.getInstance().getConfig().getString("item-spawner-name"),
                        Placeholder.parsed("amount", "1"),
                        Placeholder.parsed("spawner_type", Utils.convertUpperSnakeCaseToPascalCase(entityType.name()))
                )
        );
        itemMeta.getPersistentDataContainer().set(
                NamespacedKey.fromString("ms_mob", SpawnerGenz.getInstance()),
                PersistentDataType.STRING,
                entityType.name()
        );

        item.setItemMeta(itemMeta);

        item.setAmount(amount);
        target.getInventory().addItem(item);
    }
}
