package arthessia.featurebox.objects;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CustomEnchant {

    private String name;
    private int level;

    public static CustomEnchant fromMap(Map<String, Object> map) {
        String name = (String) map.get("name");
        int level = (map.get("level") == null) ? 0 : Integer.valueOf("" + map.get("level"));
        return new CustomEnchant(name, level);
    }

}
