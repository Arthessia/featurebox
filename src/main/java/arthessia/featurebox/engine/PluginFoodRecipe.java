package arthessia.featurebox.engine;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import arthessia.featurebox.Plugin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PluginFoodRecipe implements Listener {

    private final Plugin plugin;

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();
        if (item == null || !item.hasItemMeta())
            return;

        ItemMeta meta = item.getItemMeta();

        // check if the item is one of our custom recipes (if it starts with
        // featurebox_)
        if (meta.getPersistentDataContainer().getKeys().stream()
                .noneMatch(k -> k.getKey().startsWith("featurebox_"))) {
            return;
        }

        for (String recipeKey : plugin.getConfig().getConfigurationSection("recipes").getKeys(false)) {
            NamespacedKey idKey = new NamespacedKey(plugin, "featurebox_" + recipeKey + "_id");
            if (!meta.getPersistentDataContainer().has(idKey, PersistentDataType.INTEGER)) {
                continue;
            }
            List<Map<?, ?>> effects = plugin.getConfig().getMapList("recipes." + recipeKey + ".effects");
            if (effects != null) {
                Player p = e.getPlayer();
                for (Map<?, ?> eff : effects) {
                    String effName = eff.get("key").toString().toLowerCase(Locale.ROOT);
                    NamespacedKey nsKey = NamespacedKey.minecraft(effName);

                    PotionEffectType type = org.bukkit.Registry.EFFECT.get(nsKey);
                    if (type == null) {
                        plugin.getLogger().warning("PotionEffectType inconnu: " + effName);
                        continue;
                    }

                    int duration = 60;
                    int amplifier = 0;
                    if (eff.containsKey("duration")) {
                        duration = Integer.parseInt(eff.get("duration").toString());
                    }
                    if (eff.containsKey("amplifier")) {
                        amplifier = Integer.parseInt(eff.get("amplifier").toString());
                    }
                    p.addPotionEffect(new PotionEffect(type, duration * 20, amplifier));
                }
            }
            break;
        }
    }
}
