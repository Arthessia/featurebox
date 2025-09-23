package arthessia.featurebox.objects;

import java.io.Serializable;
import java.util.List;

public record CustomStuff(String material, List<CustomEnchant> enchants) implements Serializable {

}
