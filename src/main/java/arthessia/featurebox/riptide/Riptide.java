package arthessia.featurebox.riptide;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import arthessia.featurebox.Plugin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Riptide implements Listener {

    private final Plugin plugin;

    @EventHandler
    public void onTridentUse(PlayerInteractEvent event) {
        if (!plugin.getConfig().getBoolean("custom.riptide.enabled"))
            return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.TRIDENT || item.getItemMeta() == null)
            return;

        ItemMeta meta = item.getItemMeta();
        if (!"Poseidon".equals(meta.getDisplayName()))
            return; // Nom exact

        // Niveaux d'enchantements
        int unbreaking = meta.hasEnchant(Enchantment.UNBREAKING) ? meta.getEnchantLevel(Enchantment.UNBREAKING) : 0;
        int riptide = meta.hasEnchant(Enchantment.RIPTIDE) ? meta.getEnchantLevel(Enchantment.RIPTIDE) : 0;

        if (riptide <= 0)
            return;

        // Gestion durabilité
        if (meta instanceof Damageable damageable) {
            if (unbreaking < Plugin.RANDOM.nextInt(7)) {
                damageable.setDamage(damageable.getDamage() + (4 - unbreaking));
                if ((Material.TRIDENT.getMaxDurability() - damageable.getDamage()) < 0) {
                    event.getPlayer().getInventory().removeItem(item);
                    event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ITEM_SHIELD_BREAK, 1, 1);
                    return;
                }
                item.setItemMeta(damageable);
            }
        }

        double baseSpeed = plugin.getConfig().getDouble("custom.riptide.speed");
        double speed = baseSpeed * riptide;

        event.setCancelled(true);

        // Propulsion
        Vector direction = event.getPlayer().getLocation().getDirection().normalize().multiply(speed);
        event.getPlayer().setVelocity(direction);

        // Son Riptide + animation
        event.getPlayer().spawnParticle(Particle.CRIT, event.getPlayer().getLocation(), 100, 1, 1, 1, 0.1);
        event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1.0f,
                1.0f);
        event.getPlayer().swingMainHand();
    }
}
