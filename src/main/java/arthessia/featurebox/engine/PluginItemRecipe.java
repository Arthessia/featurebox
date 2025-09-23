package arthessia.featurebox.engine;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PluginItemRecipe implements Listener {

    private final Plugin plugin;

    @EventHandler
    public void enderWand(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null || !item.hasItemMeta())
            return;

        ItemMeta meta = item.getItemMeta();
        NamespacedKey idKey = new NamespacedKey(plugin, "ender_wand_id");
        if (!meta.getPersistentDataContainer().has(idKey, PersistentDataType.INTEGER)) {
            return;
        }
        if (!e.getAction().toString().contains("RIGHT_CLICK")) {
            return;
        }
        Player p = e.getPlayer();
        int distance = plugin.getConfig().getInt("ender_wand.distance");
        int maxUsage = plugin.getConfig().getInt("ender_wand.max_usage");
        Block target = p.getTargetBlockExact(distance);
        if (target == null) {
            return;
        }

        Location dest = target.getLocation().add(0, 1, 0);
        dest.setPitch(p.getLocation().getPitch());
        dest.setYaw(p.getLocation().getYaw());

        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        p.teleport(dest);
        p.getWorld().playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        p.getWorld().spawnParticle(Particle.PORTAL, dest, 40, 0.5, 1, 0.5, 0.1);

        NamespacedKey key = new NamespacedKey(plugin, "ender_wand_uses");

        int uses = meta.getPersistentDataContainer().getOrDefault(key,
                org.bukkit.persistence.PersistentDataType.INTEGER, 0);
        uses++;

        if (uses >= maxUsage) {
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            p.getInventory().removeItem(item);
        } else {
            meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.INTEGER, uses);
            meta.setLore(List.of(
                    "§7Clic droit : téléportation",
                    "§7Durabilité : " + (maxUsage - uses)));
            item.setItemMeta(meta);
        }

        e.setCancelled(true);
    }
}
