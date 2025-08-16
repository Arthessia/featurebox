package arthessia.featurebox.objects;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Data {
	private Map<String, PlayerCount> playerCounts = new HashMap<>();
}