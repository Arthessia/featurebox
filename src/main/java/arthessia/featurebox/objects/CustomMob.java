package arthessia.featurebox.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CustomMob implements Serializable {

    private String name;
    private String replaceEntity;
    private List<CustomEnchant> armorEnchants;
    private List<CustomEnchant> weaponEnchants;
    private String stuff;
    private double attackDamage;
    private double health;
    private double speed;
    private double spawnChance;

    @SuppressWarnings("unchecked")
    public static CustomMob fromMap(Map<String, Object> map) {
        String name = (String) map.get("name");
        String replaceEntity = (String) map.get("replaceEntity");

        // armor enchants
        List<Map<String, Object>> enchantArmorMap = (map.get("armorEnchants") == null)
                ? new ArrayList<>()
                : (List<Map<String, Object>>) map.get("armorEnchants");
        List<CustomEnchant> armorEnchants = new ArrayList<>();
        for (Map<String, Object> enchant : enchantArmorMap) {
            CustomEnchant enchantement = CustomEnchant.fromMap(enchant);
            armorEnchants.add(enchantement);
        }

        // weapon enchants
        List<Map<String, Object>> enchantWeaponMap = (map.get("weaponEnchants") == null)
                ? new ArrayList<>()
                : (List<Map<String, Object>>) map.get("weaponEnchants");
        List<CustomEnchant> weaponEnchants = new ArrayList<>();
        for (Map<String, Object> enchant : enchantWeaponMap) {
            CustomEnchant enchantement = CustomEnchant.fromMap(enchant);
            weaponEnchants.add(enchantement);
        }

        String stuff = (String) map.get("stuff");
        double attackDamage = (map.get("attackDamage") == null) ? 0d : Double.valueOf("" + map.get("attackDamage"));
        double health = (map.get("health") == null) ? 0d : Double.valueOf("" + map.get("health"));
        double speed = (map.get("speed") == null) ? 0d : Double.valueOf("" + map.get("speed"));
        double spawnChance = (map.get("spawnChance") == null) ? 5d : Double.valueOf("" + map.get("spawnChance"));
        return new CustomMob(name, replaceEntity, armorEnchants, weaponEnchants, stuff, attackDamage,
                health, speed, spawnChance);
    }
}
