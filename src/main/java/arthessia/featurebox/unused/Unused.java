package arthessia.featurebox.unused;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Illusioner;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.entity.Rabbit.Type;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import arthessia.featurebox.Plugin;

public class Unused implements Listener {

    private final Plugin plugin;

    public Unused(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ZombieHorse) {
            ZombieHorse zombieHorse = (ZombieHorse) event.getRightClicked();
            zombieHorse.setTamed(true);
            zombieHorse.setOwner(event.getPlayer());
        }
    }

    @EventHandler
    public void updateChunckEntities(ChunkLoadEvent event) {

        // bunnies
        if (plugin.getConfig().getBoolean("unused.rabbit.spawn.enabled")) {
            List<Rabbit> rabbits = Arrays.asList(event.getChunk().getEntities()).stream()
                    .filter(entity -> entity instanceof Rabbit)
                    .map(entity -> (Rabbit) entity)
                    .collect(Collectors.toList());
            for (Rabbit rabbit : rabbits) {
                if (Plugin.RANDOM.nextInt(100) <= plugin.getConfig().getInt("unused.rabbit.spawn.chance")) {
                    rabbit.setRabbitType(Type.THE_KILLER_BUNNY);
                }
            }
        }

        // horses
        if (plugin.getConfig().getBoolean("unused.zombiehorse.spawn.enabled")) {
            List<ZombieHorse> zombiehorses = Arrays.asList(event.getChunk().getEntities()).stream()
                    .filter(entity -> entity instanceof ZombieHorse)
                    .map(entity -> (ZombieHorse) entity)
                    .collect(Collectors.toList());
            List<Horse> horses = Arrays.asList(event.getChunk().getEntities()).stream()
                    .filter(entity -> entity instanceof Horse)
                    .map(entity -> (Horse) entity)
                    .filter(horse -> horse.getInventory().getSaddle() == null || !horse.isTamed())
                    .collect(Collectors.toList());
            for (Horse horse : horses) {
                if ((plugin.getConfig().getInt("unused.zombiehorse.spawn.limit") == -1
                        || zombiehorses.size() < plugin.getConfig().getInt("unused.zombiehorse.spawn.limit"))
                        && Plugin.RANDOM.nextInt(100) <= plugin.getConfig().getInt("unused.zombiehorse.spawn.chance")) {
                    // Convertir le cheval en cheval zombie
                    ZombieHorse zombieHorse = (ZombieHorse) horse.getWorld().spawnEntity(horse.getLocation(),
                            org.bukkit.entity.EntityType.ZOMBIE_HORSE);

                    // Obtenir et régler l'attribut de santé maximale
                    double maxHealth = horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                    zombieHorse.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
                    zombieHorse.setAge(horse.getAge());

                    // Optionnel : Copier d'autres attributs selon les besoins
                    zombieHorse.setJumpStrength(((Horse) horse).getJumpStrength());
                }
            }
        }
    }

    @EventHandler
    public void updateCreatureSpawn(CreatureSpawnEvent event) {
        if (plugin.getConfig().getBoolean("unused.illusioner.spawn.enabled")) {
            if (event.getEntityType() == EntityType.PILLAGER) {
                if (Plugin.RANDOM.nextInt(100) <= plugin.getConfig().getInt("unused.illusioner.spawn.chance")) {

                    // Faire apparaître un illusioner à la même position
                    Illusioner illusioner = (Illusioner) event.getEntity().getWorld().spawnEntity(
                            event.getEntity().getLocation(),
                            EntityType.ILLUSIONER);
                    illusioner.teleport(event.getEntity().getLocation());
                }
            }
        }
    }
}
