package net.mvndicraft.treefalls;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

@CommandAlias("treefalls|tf")
public class TreeFallsCommand extends BaseCommand {
    private static final String ADMIN_PERMISSION = "treefalls.admin";
    @Default
    @Description("Lists the version of the plugin")
    public static void onRsm(CommandSender commandSender) {
        commandSender.sendMessage(Component.text(TreeFallsPlugin.getInstance().toString()));
    }

    @Subcommand("reload")
    @Description("Reloads the plugin config and data file")
    @CommandPermission(ADMIN_PERMISSION)
    public static void onReload(CommandSender commandSender) {
        TreeFallsPlugin.getInstance().reloadConfig();
        commandSender.sendMessage(Component.text("TreeFalls reloaded"));
    }
}
