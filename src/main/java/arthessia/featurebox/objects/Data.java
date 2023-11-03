package arthessia.featurebox.objects;

import java.util.HashMap;
import java.util.Map;

public class Data {
	private Map<String, PlayerCount> playerCounts = new HashMap<>();

	public Map<String, PlayerCount> getPlayerCounts() {
		return playerCounts;
	}

	public void setPlayerCounts(HashMap<String, PlayerCount> playerCounts) {
		this.playerCounts = playerCounts;
	}
}