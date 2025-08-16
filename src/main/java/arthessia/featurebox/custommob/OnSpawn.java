// package arthessia.featurebox.custommob;

// import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collectors;

// import org.bukkit.Material;
// import org.bukkit.NamespacedKey;
// import org.bukkit.Registry;
// import org.bukkit.attribute.Attribute;
// import org.bukkit.enchantments.Enchantment;
// import org.bukkit.entity.EntityType;
// import org.bukkit.entity.LivingEntity;
// import org.bukkit.event.EventHandler;
// import org.bukkit.event.Listener;
// import org.bukkit.event.entity.CreatureSpawnEvent;
// import org.bukkit.inventory.ItemStack;

// import arthessia.featurebox.Plugin;
// import arthessia.featurebox.objects.CustomEnchant;
// import arthessia.featurebox.objects.CustomMob;
// import lombok.RequiredArgsConstructor;

// @RequiredArgsConstructor
// public class OnSpawn implements Listener {

//     private final Plugin plugin;

//     @EventHandler
//     @SuppressWarnings("unchecked")
//     public void spawnCustomMob(CreatureSpawnEvent event) {
//         if (plugin.getConfig().getBoolean("custom.mobs.enabled")) {
//             List<Map<String, Object>> mobMaps = (List<Map<String, Object>>) plugin.getConfig()
//                     .getList("custom.mobs.list");
//             List<CustomMob> mobs = new ArrayList<>();
//             for (Map<String, Object> mobMap : mobMaps) {
//                 CustomMob mob = CustomMob.fromMap(mobMap);
//                 mobs.add(mob);
//             }
//             Map<String, List<CustomMob>> mobMap = mobs.stream()
//                     .filter(mob -> mob.getReplaceEntity() != null)
//                     .collect(Collectors.groupingBy(CustomMob::getReplaceEntity));
//             LivingEntity entity = event.getEntity();
//             EntityType type = event.getEntityType();
//             NamespacedKey ns = type.getKeyOrNull();
//             String key = (ns == null) ? null : type.getKeyOrNull().getNamespace() + ":" + type.getKeyOrNull().getKey();
//             if (key != null && mobMap.containsKey(key)) {
//                 List<CustomMob> sortedMobs = mobMap.get(key).stream()
//                         .sorted(Comparator.comparingDouble(CustomMob::getSpawnChance))
//                         .collect(Collectors.toList());
//                 for (CustomMob mob : sortedMobs) {
//                     if (Plugin.RANDOM.nextDouble(100d) <= mob.getSpawnChance()) {
//                         if (mob.getName() != null) {
//                             entity.setCustomName(mob.getName());
//                         }
//                         if (mob.getStuff() != null) {
//                             setStuff(mob, entity);
//                         }
//                         if (mob.getHealth() > 0d) {
//                             entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(mob.getHealth());
//                             entity.setHealth(mob.getHealth());
//                         }
//                         if (mob.getSpeed() > 0d) {
//                             entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(mob.getHealth());
//                             entity.setHealth(mob.getHealth());
//                         }
//                         if (mob.getAttackDamage() > 0d) {
//                             entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(mob.getSpeed());
//                         }
//                         if (!mob.getArmorEnchants().isEmpty()) {
//                             setArmorEnchants(entity, mob);
//                         }
//                         if (!mob.getWeaponEnchants().isEmpty()) {
//                             setWeaponEnchants(entity, mob);
//                         }
//                         break;
//                     }
//                 }
//             }
//         }
//     }

//     private void setWeaponEnchants(LivingEntity entity, CustomMob mob) {
//         for (CustomEnchant enchant : mob.getWeaponEnchants()) {
//             NamespacedKey key = NamespacedKey.fromString(enchant.getName());
//             Enchantment e = Registry.ENCHANTMENT.get(key);
//             if (e != null) {
//                 if (entity.getEquipment().getItemInMainHand() != null) {
//                     ItemStack equipment = entity.getEquipment().getItemInMainHand();
//                     equipment.addEnchantment(e, enchant.getLevel());
//                     entity.getEquipment().setItemInMainHand(equipment);
//                 }
//             } else {
//                 plugin.getLogger().warning(enchant.getName() + " is not a correct ENCHANTMENT.");
//             }
//         }
//     }

//     private void setArmorEnchants(LivingEntity entity, CustomMob mob) {
//         for (CustomEnchant enchant : mob.getArmorEnchants()) {
//             NamespacedKey key = NamespacedKey.fromString(enchant.getName());
//             Enchantment e = Registry.ENCHANTMENT.get(key);
//             if (e != null) {
//                 if (entity.getEquipment().getHelmet() != null) {
//                     ItemStack equipment = entity.getEquipment().getHelmet();
//                     equipment.addEnchantment(e, enchant.getLevel());
//                     entity.getEquipment().setHelmet(equipment);
//                 }
//                 if (entity.getEquipment().getChestplate() != null) {
//                     ItemStack equipment = entity.getEquipment().getChestplate();
//                     equipment.addEnchantment(e, enchant.getLevel());
//                     entity.getEquipment().setChestplate(equipment);
//                 }
//                 if (entity.getEquipment().getLeggings() != null) {
//                     ItemStack equipment = entity.getEquipment().getLeggings();
//                     equipment.addEnchantment(e, enchant.getLevel());
//                     entity.getEquipment().setLeggings(equipment);
//                 }
//                 if (entity.getEquipment().getBoots() != null) {
//                     ItemStack equipment = entity.getEquipment().getBoots();
//                     equipment.addEnchantment(e, enchant.getLevel());
//                     entity.getEquipment().setBoots(equipment);
//                 }
//             } else {
//                 plugin.getLogger().warning(enchant.getName() + " is not a correct ENCHANTMENT.");
//             }
//         }
//     }

//     private void setStuff(CustomMob mob, LivingEntity entity) {
//         try {
//             for (String stuff : mob.getStuff().split(",")) {
//                 if (stuff.toUpperCase().contains("SWORD")
//                         || stuff.toUpperCase().contains("AXE")
//                         || stuff.toUpperCase().contains("BOW")
//                         || stuff.toUpperCase().contains("SHOVEL")
//                         || stuff.toUpperCase().contains("HOE")
//                         || stuff.toUpperCase().contains("PICKAXE")) {
//                     entity.getEquipment().setItemInMainHand(new ItemStack(Material.valueOf(stuff.toUpperCase())));
//                 }
//                 if (stuff.toUpperCase().contains("HELMET")) {
//                     entity.getEquipment().setHelmet(new ItemStack(Material.valueOf(stuff.toUpperCase())));
//                 }
//                 if (stuff.toUpperCase().contains("CHESTPLATE")) {
//                     entity.getEquipment().setChestplate(new ItemStack(Material.valueOf(stuff.toUpperCase())));
//                 }
//                 if (stuff.toUpperCase().contains("LEGGINGS")) {
//                     entity.getEquipment().setLeggings(new ItemStack(Material.valueOf(stuff.toUpperCase())));
//                 }
//                 if (stuff.toUpperCase().contains("BOOTS")) {
//                     entity.getEquipment().setBoots(new ItemStack(Material.valueOf(stuff.toUpperCase())));
//                 }
//             }
//         } catch (IllegalArgumentException | NullPointerException e) {
//             plugin.getLogger().warning("Error during stuff implementation on " + mob.getName() + " ["
//                     + mob.getReplaceEntity() + "] " + e.getMessage());
//         }
//     }
// }
