package arthessia.featurebox.ondeath;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import arthessia.featurebox.Plugin;

public class OnDeath implements Listener {

    private final Plugin plugin;

    public OnDeath(Plugin plugin) {
        this.plugin = plugin;
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
}
