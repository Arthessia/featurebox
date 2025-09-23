package arthessia.featurebox.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import arthessia.featurebox.Plugin;
import lombok.RequiredArgsConstructor;

public class RiptideCommands {

    @RequiredArgsConstructor
    public static class RiptideEnabled implements CommandExecutor {

        private final Plugin plugin;

        @Override
        public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
            if (sender.hasPermission("arthessia.featurebox.riptidetoggle")) {
                plugin.getConfig().set("riptide.enabled",
                        (plugin.getConfig().getBoolean("riptide.enabled")) ? false : true);
                plugin.saveConfig();
                sender.sendMessage((plugin.getConfig().getBoolean("riptide.enabled"))
                        ? "Riptide is now enabled."
                        : "Riptide is now disabled.");
                return true;
            } else {
                sender.sendMessage("You don't have the permission.");
                return false;
            }
        }
    }

    public static class RiptideForce implements CommandExecutor {

        private final Plugin plugin;

        public RiptideForce(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
            if (sender.hasPermission("arthessia.featurebox.riptideforce") &&
                    args.length > 0) {
                try {
                    plugin.getConfig().set("riptide.speed", Double.parseDouble(args[0]));
                } catch (NumberFormatException e) {
                    sender.sendMessage("You must set a number.");
                    return false;
                }
                plugin.saveConfig();
                return true;
            } else {
                sender.sendMessage("You need to specify speed and have the correct permission.");
                return false;
            }
        }
    }
}
