package arthessia.featurebox.unused;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Illusioner;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.entity.Rabbit.Type;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import arthessia.featurebox.Plugin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Unused implements Listener {

    private final Plugin plugin;

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
                    double maxHealth = horse.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
                    zombieHorse.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
                    zombieHorse.setAge(horse.getAge());

                    // Domestication test
                    zombieHorse.setDomestication(horse.getDomestication());
                    zombieHorse.setBreed(horse.canBreed());
                    zombieHorse.setTamed(horse.isTamed());
                    if (horse.isTamed() && horse.getOwner() != null) {
                        zombieHorse.setOwner(horse.getOwner());
                    }

                    // Optionnel : Copier d'autres attributs selon les besoins
                    zombieHorse.setJumpStrength(((Horse) horse).getJumpStrength());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ZombieHorse zombieHorse))
            return;

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();

        if (!zombieHorse.isTamed()) {
            if (hand.getType() == Material.ROTTEN_FLESH) {
                int randomInt = Plugin.RANDOM.nextInt(100);
                int chance = plugin.getConfig().getInt("unused.zombiehorse.taming.chance", 33);

                if (randomInt <= chance) {
                    zombieHorse.setTamed(true);
                    zombieHorse.setOwner(player);
                    zombieHorse.getWorld().playSound(zombieHorse.getLocation(), Sound.ENTITY_HORSE_BREATHE, 1f, 1f);
                    zombieHorse.getWorld().spawnParticle(Particle.HEART, zombieHorse.getLocation().add(0, 1, 0), 5);
                } else {
                    zombieHorse.getWorld().playSound(zombieHorse.getLocation(), Sound.ENTITY_HORSE_EAT, 1f, 1f);
                }

                hand.setAmount(hand.getAmount() - 1);
            } else if (hand.getType() == Material.SADDLE) {
                zombieHorse.getWorld().playSound(zombieHorse.getLocation(), Sound.ENTITY_HORSE_ANGRY, 1f, 1f);
                zombieHorse.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, zombieHorse.getLocation().add(0, 1, 0), 3,
                        0.2, 0.5, 0.2, 0.01);
                zombieHorse.setVelocity(zombieHorse.getVelocity().add(new Vector(0, 0.2, 0)));
            } else {
                zombieHorse.getWorld().playSound(zombieHorse.getLocation(), Sound.ENTITY_HORSE_ANGRY, 1f, 1f);
                zombieHorse.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, zombieHorse.getLocation().add(0, 1, 0), 3,
                        0.2, 0.5, 0.2, 0.01);
                zombieHorse.setVelocity(zombieHorse.getVelocity().add(new Vector(0, 0.2, 0)));
            }

            event.setCancelled(true);
            return;
        }

        ItemStack saddle = zombieHorse.getInventory().getItem(0); // slot 0 = selle
        if ((saddle == null || saddle.getType() != Material.SADDLE) && hand.getType() == Material.SADDLE) {
            zombieHorse.getInventory().setItem(0, new ItemStack(Material.SADDLE));
            hand.setAmount(hand.getAmount() - 1);
            event.setCancelled(true);
            return;
        }

        if (saddle != null && saddle.getType() == Material.SADDLE) {
            zombieHorse.addPassenger(player);
            event.setCancelled(true);
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
