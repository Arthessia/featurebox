package arthessia.featurebox.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomMob implements Serializable {

    private String name;
    private String replaceEntity;
    private List<CustomStuff> stuff;
    private double attackDamage;
    private double health;
    private double speed;
    private double spawnChance;

    @SuppressWarnings("unchecked")
    public static CustomMob fromMap(Map<String, Object> map) {
        CustomMob mob = new CustomMob();

        mob.setName((String) map.get("name"));
        mob.setReplaceEntity((String) map.get("replaceEntity"));

        // ---- Stuff ----
        List<CustomStuff> stuffList = new ArrayList<>();
        List<Map<String, Object>> rawStuff = (List<Map<String, Object>>) map.getOrDefault("stuff", new ArrayList<>());
        for (Map<String, Object> raw : rawStuff) {
            String material = (String) raw.get("material");
            List<CustomEnchant> enchants = new ArrayList<>();
            List<Map<String, Object>> rawEnchants = (List<Map<String, Object>>) raw.getOrDefault("enchants",
                    new ArrayList<>());
            for (Map<String, Object> e : rawEnchants) {
                enchants.add(CustomEnchant.fromMap(e));
            }
            stuffList.add(new CustomStuff(material, enchants));
        }
        mob.setStuff(stuffList);

        mob.setAttackDamage(Double.parseDouble("" + map.getOrDefault("attackDamage", "0")));
        mob.setHealth(Double.parseDouble("" + map.getOrDefault("health", "0")));
        mob.setSpeed(Double.parseDouble("" + map.getOrDefault("speed", "0")));
        mob.setSpawnChance(Double.parseDouble("" + map.getOrDefault("spawnChance", "5")));

        return mob;
    }
}
