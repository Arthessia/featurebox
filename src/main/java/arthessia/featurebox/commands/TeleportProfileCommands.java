package arthessia.featurebox.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import arthessia.featurebox.Plugin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TeleportProfileCommands implements CommandExecutor, TabCompleter {

    private final Plugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player))
            return true;

        if (args.length < 2 || !args[0].equalsIgnoreCase("profile")) {
            player.sendMessage(
                    "§cUsage: /featurebox profile <sound_ambient|sound_teleport|particle|lightning> <value>");
            return true;
        }

        String option = args[1].toLowerCase();
        String basePath = "teleport.profiles." + player.getName();

        switch (option) {
            case "sound_ambient":
            case "sound_teleport":
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /featurebox profile " + option + " <sound>");
                    return true;
                }
                String soundName = args[2].toLowerCase();
                NamespacedKey soundKey = NamespacedKey.minecraft(soundName);
                if (Registry.SOUNDS.get(soundKey) == null) {
                    player.sendMessage("§cInvalid sound: " + soundName);
                    return true;
                }
                plugin.getConfig().set(basePath + (option.equals("sound_ambient") ? ".ambient" : ".teleport"),
                        soundName);
                plugin.saveConfig();
                player.sendMessage("§a" + option.replace('_', ' ') + " set to: " + soundName);
                break;

            case "particle":
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /featurebox profile particle <particle>");
                    return true;
                }
                String particleName = args[2].toLowerCase();
                NamespacedKey particleKey = NamespacedKey.minecraft(particleName);
                if (Registry.PARTICLE_TYPE.get(particleKey) == null) {
                    player.sendMessage("§cInvalid particle: " + particleName);
                    return true;
                }
                plugin.getConfig().set(basePath + ".particle", particleName);
                plugin.saveConfig();
                player.sendMessage("§aParticle set to: " + particleName);
                break;

            case "lightning":
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /featurebox profile lightning <true|false>");
                    return true;
                }
                String boolArg = args[2].toLowerCase();
                if (!boolArg.equals("true") && !boolArg.equals("false")) {
                    player.sendMessage("§cInvalid value. Use true or false.");
                    return true;
                }
                boolean lightning = Boolean.parseBoolean(boolArg);
                plugin.getConfig().set(basePath + ".lightning", lightning);
                plugin.saveConfig();
                player.sendMessage("§aLightning set to: " + lightning);
                break;

            default:
                player.sendMessage("§cInvalid option. Use sound_ambient|sound_teleport|particle|lightning");
                break;
        }

        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("profile");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("profile")) {
            return Arrays.asList("sound_ambient", "sound_teleport", "particle", "lightning");
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("profile")) {
            switch (args[1].toLowerCase()) {
                case "sound_ambient":
                case "sound_teleport":
                    return Registry.SOUNDS.stream()
                            .map(key -> key.getKey())
                            .filter(key -> key != null)
                            .map(NamespacedKey::getKey)
                            .filter(s -> s.startsWith(args[2].toLowerCase()))
                            .toList();

                case "particle":
                    return Registry.PARTICLE_TYPE.stream()
                            .map(key -> key.getKey())
                            .filter(key -> key != null)
                            .map(NamespacedKey::getKey)
                            .filter(s -> s.startsWith(args[2].toLowerCase()))
                            .toList();

                case "lightning":
                    return Arrays.asList("true", "false");
            }
        }
        return Collections.emptyList();
    }
}
