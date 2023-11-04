package arthessia.featurebox.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import arthessia.featurebox.Plugin;

public class UnusedCommands {

    public static class UnusedSpawn implements CommandExecutor {

        private final Plugin plugin;

        public UnusedSpawn(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
            if (sender.hasPermission("arthessia.featurebox.unusedtoggle") &&
                    args.length > 0) {
                switch (args[0]) {
                    case "rabbit":
                        toggleEntity(sender, args);
                        break;

                    case "zombiehorse":
                        toggleEntity(sender, args);
                        break;

                    case "illusioner":
                        toggleEntity(sender, args);
                        break;

                    default:
                        sender.sendMessage("Entity not found.");
                        return false;
                }
            } else {
                sender.sendMessage(
                        "You need to specify an Entity name. (rabbit, zombiehorse, illusioner) and have correct permission.");
                return false;
            }
            return true;
        }

        private void toggleEntity(CommandSender sender, String[] args) {
            plugin.getConfig().set("unused." + args[0] + ".spawn.enabled",
                    (plugin.getConfig().getBoolean("unused." + args[0] + ".spawn.enabled"))
                            ? false
                            : true);
            sender.sendMessage((plugin.getConfig().getBoolean("unused." + args[0] + ".spawn.enabled"))
                    ? args[0] + " is now enabled."
                    : args[0] + " is now disabled.");
            plugin.saveConfig();
        }
    }

    public static class UnusedChance implements CommandExecutor {

        private final Plugin plugin;

        public UnusedChance(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
            if (sender.hasPermission("arthessia.featurebox.unusedchance") &&
                    args.length > 1) {
                switch (args[0]) {
                    case "rabbit":
                        chanceEntity(sender, args);
                        break;

                    case "zombiehorse":
                        chanceEntity(sender, args);
                        break;

                    case "illusioner":
                        chanceEntity(sender, args);
                        break;

                    default:
                        sender.sendMessage("Entity not found.");
                        return false;
                }
            } else {
                sender.sendMessage(
                        "You need to specify an Entity name. (rabbit, zombiehorse, illusioner), a percent and have the correct permission.");
                return false;
            }
            return true;
        }

        private void chanceEntity(CommandSender sender, String[] args) {
            try {
                plugin.getConfig().set(
                        "unused." + args[0] + ".spawn.chance",
                        Integer.parseInt(args[1]));
                sender.sendMessage(args[0] + " has now " + args[1] + "% chance to spawn.");
                plugin.saveConfig();
            } catch (NumberFormatException e) {
                sender.sendMessage(
                        "Syntax error : You need 1) an entity name 2) an integer between 0 and 100 (spawn chance)");
            }
        }
    }

    public static class UnusedLimit implements CommandExecutor {

        private final Plugin plugin;

        public UnusedLimit(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
            if (sender.hasPermission("arthessia.featurebox.unusedlimit") &&
                    args.length > 1) {
                switch (args[0]) {

                    case "zombiehorse":
                        limitEntity(sender, args);
                        break;

                    default:
                        sender.sendMessage("Entity not found.");
                        return false;
                }
            } else {
                sender.sendMessage(
                        "You need to specify an Entity name. (zombiehorse), a limit (number) and have the correct permission.");
                return false;
            }
            return true;
        }

        private void limitEntity(CommandSender sender, String[] args) {
            try {
                plugin.getConfig().set(
                        "unused." + args[0] + ".spawn.limit",
                        Integer.parseInt(args[1]));
                sender.sendMessage(args[0] + " has now " + args[1] + " as limit of entity generated per chunk.");
                plugin.saveConfig();
            } catch (NumberFormatException e) {
                sender.sendMessage(
                        "Syntax error : You need 1) an entity name 2) an integer (number of entity limit per chunk)");
            }
        }
    }
}
