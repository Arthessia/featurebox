package arthessia.featurebox.ondeath;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import arthessia.featurebox.Plugin;

public class OnDeathCommands {

    public static class OnDeathToggle implements CommandExecutor {

        private final Plugin plugin;

        public OnDeathToggle(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
            if (sender.hasPermission("arthessia.featurebox.ondeathtoggle")) {
                plugin.getConfig().set("common.location.death.enabled",
                        (plugin.getConfig().getBoolean("common.location.death.enabled")) ? false : true);
                plugin.saveConfig();
                sender.sendMessage((plugin.getConfig().getBoolean("common.location.death.enabled"))
                        ? "Location on death is now enabled."
                        : "Location on death is now disabled.");
                return true;
            } else {
                sender.sendMessage("You don't have the permission.");
                return false;
            }
        }
    }
}
