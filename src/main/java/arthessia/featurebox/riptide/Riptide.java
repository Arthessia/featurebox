package arthessia.featurebox.riptide;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.util.Vector;

import arthessia.featurebox.Plugin;

public class Riptide implements Listener {

    private final Plugin plugin;

    public Riptide(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void poseidonInteract(PlayerInteractEvent event) {
        if (plugin.getConfig().getBoolean("custom.riptide.enabled") &&
                event.getPlayer().getInventory().getItemInMainHand() != null
                && event.getPlayer().getInventory().getItemInMainHand().getItemMeta() != null
                && event.getPlayer().getInventory().getItemInMainHand().getType() != null
                && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.TRIDENT
                && event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName()
                        .equals("Poseidon")
                && event.getAction() == Action.RIGHT_CLICK_AIR) {

            Damageable meta = (Damageable) event.getPlayer().getInventory().getItemInMainHand().getItemMeta();
            int unbreaking = (meta.hasEnchant(Enchantment.DURABILITY))
                    ? meta.getEnchantLevel(Enchantment.DURABILITY)
                    : 0;
            int riptide = (meta.hasEnchant(Enchantment.RIPTIDE))
                    ? meta.getEnchantLevel(Enchantment.RIPTIDE)
                    : 0;

            if (unbreaking < Plugin.RANDOM.nextInt(7)) {
                meta.setDamage(meta.getDamage() + (4 - unbreaking));
                if ((Material.TRIDENT.getMaxDurability() - meta.getDamage() < 0)) {
                    event.getPlayer().getInventory().removeItem(event.getPlayer().getInventory().getItemInMainHand());
                    event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ITEM_SHIELD_BREAK, 1, 1);
                }
                event.getPlayer().getInventory().getItemInMainHand().setItemMeta(meta);
            }
            event.getPlayer().spawnParticle(Particle.CRIT_MAGIC, event.getPlayer().getLocation(),
                    100, 1, 1, 1, 0.1);
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ITEM_TRIDENT_THROW, 1.0F, 1.0F);
            double speed = plugin.getConfig().getDouble("custom.riptide.speed") + (riptide);
            Vector direction = event.getPlayer().getLocation().getDirection().multiply(speed);
            event.getPlayer().setVelocity(direction);
        }
    }
}
