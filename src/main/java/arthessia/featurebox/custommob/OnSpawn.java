package arthessia.featurebox.custommob;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import arthessia.featurebox.Plugin;
import arthessia.featurebox.objects.CustomEnchant;
import arthessia.featurebox.objects.CustomMob;
import arthessia.featurebox.objects.CustomStuff;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OnSpawn implements Listener {

    private final Plugin plugin;

    @EventHandler
    @SuppressWarnings({ "unchecked", "deprecation" })
    public void spawnCustomMob(CreatureSpawnEvent event) {
        if (plugin.getConfig().getBoolean("mobs.enabled")) {
            List<Map<String, Object>> mobMaps = (List<Map<String, Object>>) plugin.getConfig()
                    .getList("mobs.list");
            List<CustomMob> mobs = new ArrayList<>();
            for (Map<String, Object> mobMap : mobMaps) {
                CustomMob mob = CustomMob.fromMap(mobMap);
                mobs.add(mob);
            }
            Map<String, List<CustomMob>> mobMap = mobs.stream()
                    .filter(mob -> mob.getReplaceEntity() != null)
                    .collect(Collectors.groupingBy(CustomMob::getReplaceEntity));
            LivingEntity entity = event.getEntity();
            EntityType type = event.getEntityType();
            NamespacedKey ns = null;
            try {
                ns = NamespacedKey.fromString(type.getKey().toString());
            } catch (Exception e) {
                plugin.getLogger().warning("Error during NamespacedKey creation for " + type.name() + " "
                        + e.getMessage());
            }
            String key = (ns == null) ? null : type.getKey().getNamespace() + ":" + type.getKey().getKey();
            if (key != null && mobMap.containsKey(key)) {
                List<CustomMob> sortedMobs = mobMap.get(key).stream()
                        .sorted(Comparator.comparingDouble(CustomMob::getSpawnChance))
                        .collect(Collectors.toList());
                for (CustomMob mob : sortedMobs) {
                    if (Plugin.RANDOM.nextDouble(100d) <= mob.getSpawnChance()) {
                        if (mob.getName() != null) {
                            entity.setCustomName(mob.getName());
                        }
                        if (mob.getStuff() != null && !mob.getStuff().isEmpty()) {
                            setStuff(mob, entity);
                        }
                        if (mob.getHealth() > 0d) {
                            entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(mob.getHealth());
                            entity.setHealth(mob.getHealth());
                        }
                        if (mob.getSpeed() > 0d) {
                            entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(mob.getSpeed());
                        }
                        if (mob.getAttackDamage() > 0d) {
                            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(mob.getAttackDamage());
                        }
                        break;
                    }
                }
            }
        }
    }

    private void setStuff(CustomMob mob, LivingEntity entity) {
        try {
            for (CustomStuff s : mob.getStuff()) {
                Material mat = Material.matchMaterial(s.material());
                if (mat == null)
                    continue;

                ItemStack item = new ItemStack(mat);
                // ajouter les enchants propres à l’item
                if (s.enchants() != null) {
                    for (CustomEnchant ce : s.enchants()) {
                        NamespacedKey key = NamespacedKey.fromString(ce.getName());
                        Enchantment ench = Registry.ENCHANTMENT.get(key);
                        if (ench != null) {
                            item.addUnsafeEnchantment(ench, ce.getLevel());
                        }
                    }
                }

                // placer dans le bon slot
                String upper = mat.name().toUpperCase();
                if (upper.contains("SWORD") || upper.contains("AXE")
                        || upper.contains("BOW") || upper.contains("CROSSBOW")
                        || upper.contains("HOE") || upper.contains("PICKAXE")
                        || upper.contains("SHOVEL")) {
                    entity.getEquipment().setItemInMainHand(item);
                } else if (upper.contains("HELMET")) {
                    entity.getEquipment().setHelmet(item);
                } else if (upper.contains("CHESTPLATE")) {
                    entity.getEquipment().setChestplate(item);
                } else if (upper.contains("LEGGINGS")) {
                    entity.getEquipment().setLeggings(item);
                } else if (upper.contains("BOOTS")) {
                    entity.getEquipment().setBoots(item);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error while setting stuff on " + mob.getName() + ": " + e.getMessage());
        }
    }
}
