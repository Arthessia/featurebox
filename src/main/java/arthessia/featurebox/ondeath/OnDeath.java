package arthessia.featurebox.ondeath;

import java.util.ArrayList;

import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import arthessia.featurebox.Plugin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OnDeath implements Listener {

    private final Plugin plugin;

    @EventHandler
    public void undyingOnDamage(EntityDamageEvent event) {
        if (plugin.getConfig().getBoolean("common.totem.death.inventory.enabled")
                && event.getEntity() instanceof Player) {
            Player livingEntity = (Player) event.getEntity();
            if ((livingEntity.getHealth() - event.getFinalDamage()) <= 0) {
                Player player = (Player) event.getEntity();
                PlayerInventory inventory = player.getInventory();
                if (inventory != null) {
                    boolean saved = false;
                    for (ItemStack stack : inventory.getContents()) {
                        if (stack != null && stack.getType() == Material.TOTEM_OF_UNDYING) {
                            stack.setAmount(0);
                            triggerTotemEffect(player);
                            event.setCancelled(true);
                            player.sendMessage("You have been saved by your Totem of Undying.");
                            saved = true;
                        }
                        if (stack != null && stack.getType() == Material.BUNDLE && stack.hasItemMeta()) {
                            BundleMeta bundleMeta = (BundleMeta) stack.getItemMeta();
                            for (ItemStack inside : bundleMeta.getItems()) {
                                if (inside != null && inside.getType() == Material.TOTEM_OF_UNDYING) {
                                    bundleMeta.setItems(new ArrayList<>());
                                    stack.setItemMeta(bundleMeta);
                                    triggerTotemEffect(player);
                                    event.setCancelled(true);
                                    player.sendMessage("You have been saved by your Totem of Undying.");
                                    saved = true;
                                }
                            }
                        }
                        if (saved) {
                            break;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void locationOnDeath(PlayerDeathEvent event) {
        if (plugin.getConfig().getBoolean("common.location.death.enabled")) {
            event.getEntity().sendMessage("You died in " + event.getEntity().getLocation().getWorld().getName()
                    + " X: " + (int) event.getEntity().getLocation().getX()
                    + " Y: " + (int) event.getEntity().getLocation().getY()
                    + " Z: " + (int) event.getEntity().getLocation().getZ() + ".");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfig().getBoolean("common.name.death.enabled"))
            return;

        Player victim = event.getEntity();

        EntityDamageEvent damageEvent = victim.getLastDamageCause();
        if (!(damageEvent instanceof EntityDamageByEntityEvent entityEvent)) {
            return;
        }

        Entity damager = entityEvent.getDamager();

        if (damager instanceof Projectile projectile &&
                projectile.getShooter() instanceof LivingEntity shooter) {
            damager = shooter;
        }

        if (damager instanceof LivingEntity living && !(damager instanceof Player)) {
            String newName = plugin.getConfig().getString("common.name.death.template");
            if (newName == null || newName.isEmpty()) {
                return;
            }
            newName = newName.replace("{victim}", victim.getName());
            living.setCustomName(newName);
            living.setCustomNameVisible(true);
            plugin.getLogger().info(living.getType() + " renommé en \"" + newName + "\"");
        }
    }

    public void triggerTotemEffect(Player player) {
        player.playEffect(EntityEffect.TOTEM_RESURRECT);

        // Effets du totem (même que vanilla)
        player.setHealth(1.0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 900, 1)); // 45s regen II
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1)); // 5s absorption II
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0)); // 40s fire resistance

        // Jouer le son
        player.getWorld().playSound(player.getLocation(), "item.totem.use", 1.0f, 1.0f);
    }
}
