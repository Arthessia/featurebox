package arthessia.featurebox.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import arthessia.featurebox.Plugin;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CustomMobCommands {

    @RequiredArgsConstructor
    public static class CustomMobToggle implements CommandExecutor {

        private final Plugin plugin;

        @Override
        public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
            if (sender.hasPermission("arthessia.featurebox.custommobtoggle")) {
                plugin.getConfig().set("custom.mobs.enabled",
                        (plugin.getConfig().getBoolean("custom.mobs.enabled")) ? false : true);
                plugin.saveConfig();
                sender.sendMessage((plugin.getConfig().getBoolean("custom.mobs.enabled"))
                        ? "Custom mob feature is now enabled."
                        : "Custom mob feature is now disabled.");
                return true;
            } else {
                sender.sendMessage("You don't have the permission.");
                return false;
            }
        }
    }

    @RequiredArgsConstructor
    public static class FindCustomMob implements CommandExecutor {

        private final Plugin plugin;

        @Override
        public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
            if (sender.hasPermission("arthessia.featurebox.findcustommob")) {
                if (args.length == 0) {
                    sender.sendMessage("Missing argument: /findcustommob <name>");
                }
                plugin.getLogger().info("Starting search...");
                Entity entity = Bukkit.getWorlds().stream()
                        .flatMap(world -> world.getEntities().stream())
                        .filter(e -> e instanceof LivingEntity)
                        .filter(e -> e.getCustomName() != null)
                        .filter(e -> e.getCustomName().equals(args[0]))
                        .findFirst().orElse(null);
                String message = (entity == null)
                        ? "No entity found."
                        : "Entity found in " + entity.getWorld().getName() + " at " + entity.getLocation();
                String command = (entity == null)
                        ? null
                        : String.format("/tp %d %d %d",
                                entity.getLocation().getBlockX(),
                                entity.getLocation().getBlockY(),
                                entity.getLocation().getBlockZ());
                plugin.getLogger().info(message);
                if (entity == null) {
                    sender.sendMessage(message);
                } else {
                    TextComponent text = new TextComponent(message);
                    text.setColor(ChatColor.GREEN);
                    text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
                    sender.spigot().sendMessage(text);
                }

                return true;
            } else {
                sender.sendMessage("You don't have the permission.");
                return false;
            }
        }
    }
}
