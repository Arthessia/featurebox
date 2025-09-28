package arthessia.featurebox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;

import arthessia.featurebox.commands.CommonCommands.ReloadCommand;
import arthessia.featurebox.commands.CustomMobCommands.CustomMobToggle;
import arthessia.featurebox.commands.CustomMobCommands.FindCustomMob;
import arthessia.featurebox.commands.OnDeathCommands.OnDeathToggle;
import arthessia.featurebox.commands.OnDeathCommands.UndyingToggle;
import arthessia.featurebox.commands.RiptideCommands.RiptideEnabled;
import arthessia.featurebox.commands.RiptideCommands.RiptideForce;
import arthessia.featurebox.commands.UnusedCommands.UnusedChance;
import arthessia.featurebox.commands.UnusedCommands.UnusedLimit;
import arthessia.featurebox.commands.UnusedCommands.UnusedSpawn;
import arthessia.featurebox.custommob.OnSpawn;
import arthessia.featurebox.engine.PluginFoodRecipe;
import arthessia.featurebox.engine.PluginItemRecipe;
import arthessia.featurebox.objects.CustomMob;
import arthessia.featurebox.objects.Data;
import arthessia.featurebox.ondeath.OnDeath;
import arthessia.featurebox.riptide.Riptide;
import arthessia.featurebox.stones.TeleportStone;
import arthessia.featurebox.unused.Unused;

public class Plugin extends JavaPlugin implements Listener {
    private static File DATAD = new File("plugins/featurebox/data.json");
    private static Data DATA = new Data();
    public static final Random RANDOM = new Random();

    @Override
    public void onEnable() {
        getLogger().info("Featurebox loading...");
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        getLogger().info("Config loaded... ");

        this.getCommand("featurebox").setExecutor(new ReloadCommand(this));
        this.getCommand("unusedtoggle").setExecutor(new UnusedSpawn(this));
        this.getCommand("unusedchance").setExecutor(new UnusedChance(this));
        this.getCommand("unusedlimit").setExecutor(new UnusedLimit(this));
        this.getCommand("riptidetoggle").setExecutor(new RiptideEnabled(this));
        this.getCommand("riptideforce").setExecutor(new RiptideForce(this));
        this.getCommand("ondeathtoggle").setExecutor(new OnDeathToggle(this));
        this.getCommand("undyingtoggle").setExecutor(new UndyingToggle(this));
        this.getCommand("custommobtoggle").setExecutor(new CustomMobToggle(this));
        this.getCommand("findcustommob").setExecutor(new FindCustomMob(this));
        getLogger().info("Commands loaded...");

        loadFoodRecipesFromConfig();
        loadItemRecipes();
        loadCustomMobs();
        getLogger().info("Custom mobs and recipes loaded...");

        Riptide riptide = new Riptide(this);
        Unused unused = new Unused(this);
        OnDeath onDeath = new OnDeath(this);
        PluginFoodRecipe pluginFoodRecipe = new PluginFoodRecipe(this);
        PluginItemRecipe pluginItemRecipe = new PluginItemRecipe(this);
        OnSpawn onSpawn = new OnSpawn(this);
        TeleportStone stones = new TeleportStone(this);
        Bukkit.getServer().getPluginManager().registerEvents(onDeath, this);
        Bukkit.getServer().getPluginManager().registerEvents(riptide, this);
        Bukkit.getServer().getPluginManager().registerEvents(unused, this);
        Bukkit.getServer().getPluginManager().registerEvents(pluginFoodRecipe, this);
        Bukkit.getServer().getPluginManager().registerEvents(pluginItemRecipe, this);
        Bukkit.getServer().getPluginManager().registerEvents(onSpawn, this);
        if (getConfig().getBoolean("teleport.enabled", true)) {
            Bukkit.getServer().getPluginManager().registerEvents(stones, this);
            playTeleportStones();
        }
        getLogger().info("Features loaded...");
    }

    private void playTeleportStones() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            String basePath = "teleport.";
            for (String stoneKey : this.getConfig().getConfigurationSection("teleport.stones").getKeys(false)) {
                String worldName = this.getConfig().getString("teleport.stones." + stoneKey + ".world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    continue;
                }
                String soundName = this.getConfig().getString(basePath + "sound.ambient", "AMBIENT_BASALT_DELTAS_MOOD");
                String particleName = this.getConfig().getString(basePath + "particle", "end_rod");
                float volume = (float) this.getConfig().getDouble(basePath + "sound.volume", 1.0);
                float pitch = (float) this.getConfig().getDouble(basePath + "sound.pitch", 1.0);

                double x = this.getConfig().getDouble("teleport.stones." + stoneKey + ".x");
                double y = this.getConfig().getDouble("teleport.stones." + stoneKey + ".y");
                double z = this.getConfig().getDouble("teleport.stones." + stoneKey + ".z");

                if (world.getBlockAt((int) x, (int) y, (int) z).getType() != Material.LIGHTNING_ROD) {
                    continue;
                }

                String perStonePath = basePath + "stones." + stoneKey + ".sound.";
                if (this.getConfig().isConfigurationSection(perStonePath)) {
                    soundName = this.getConfig().getString(perStonePath + "ambient", soundName);
                    particleName = this.getConfig().getString(basePath + "stones." + stoneKey + ".particle",
                            particleName);
                    volume = (float) this.getConfig().getDouble(perStonePath + "volume", volume);
                    pitch = (float) this.getConfig().getDouble(perStonePath + "pitch", pitch);
                }

                Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);

                for (Player p : world.getPlayers()) {
                    if (p.getLocation().distanceSquared(loc) <= 16 * 16) { // 16 blocs de rayon
                        try {
                            NamespacedKey keySound = NamespacedKey.minecraft(soundName.toLowerCase());
                            NamespacedKey keyParticle = NamespacedKey.minecraft(particleName.toLowerCase());
                            Sound sound = Registry.SOUNDS.get(keySound);
                            Particle particle = Registry.PARTICLE_TYPE.get(keyParticle);
                            p.playSound(loc, sound, volume, pitch);
                            loc.getBlock().getWorld().spawnParticle(particle, loc, 10, 0.3, 0.3, 0.3, 0.01);
                        } catch (IllegalArgumentException ex) {
                            this.getLogger().warning("Son invalide pour la pierre " + stoneKey + ": " + soundName);
                        } catch (Exception ex) {
                            this.getLogger().warning("Son inexistant pour la pierre " + stoneKey + ": " + soundName);
                        }
                    }
                }
            }
        }, 0L, this.getConfig().getLong("teleport.sound.delay", 10) * 20L);
    }

    private ShapedRecipe createEnderWandRecipe() {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName("§5Baguette de l'End");
        meta.setLore(List.of("§7Clic droit : téléportation",
                "§7Durabilité : " + this.getConfig().getInt("ender_wand.max_usage")));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(this, "ender_wand_id"),
                PersistentDataType.INTEGER,
                1);
        wand.setItemMeta(meta);

        NamespacedKey key = new NamespacedKey(this, "ender_wand");
        ShapedRecipe recipe = new ShapedRecipe(key, wand);
        recipe.shape(" PP", " BP", "B  ");
        recipe.setIngredient('P', Material.ENDER_PEARL);
        recipe.setIngredient('B', Material.BLAZE_ROD);
        return recipe;
    }

    private void loadItemRecipes() {
        if (getConfig().getBoolean("ender_wand.enabled", true)) {
            getServer().addRecipe(createEnderWandRecipe());
            getLogger().info("Registered recipe: ender_wand");
        }
    }

    @Override
    public void saveConfig() {
        super.saveConfig();
    }

    public static void save() {
        Gson gson = new Gson();

        try (FileWriter fileWriter = new FileWriter(DATAD)) {

            fileWriter.write(gson.toJson(DATA));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Data getData() {
        return DATA;
    }

    @Override
    public FileConfiguration getConfig() {
        return super.getConfig();
    }

    @Override
    public void onDisable() {
        save();
        getLogger().info("Featurebox shutdown...");
    }

    private void loadCustomMobs() {
        List<Map<?, ?>> rawList = getConfig().getMapList("mobs.list");
        if (rawList == null || rawList.isEmpty()) {
            getLogger().warning("No custom mobs found in configuration.");
            return;
        }

        for (Map<?, ?> raw : rawList) {
            // On convertit les clés en String pour être tranquille
            Map<String, Object> map = raw.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey().toString(),
                            Map.Entry::getValue));

            CustomMob mob = CustomMob.fromMap(map);

            getLogger().info("Mob config detected: " + mob.getName()
                    + " will replace " + mob.getReplaceEntity()
                    + " with spawn rate of " + mob.getSpawnChance());
        }
    }

    public void loadFoodRecipesFromConfig() {
        if (!getConfig().contains("recipes")) {
            getLogger().info("Aucune recette trouvée dans la config.");
            return;
        }

        Set<String> recipeKeys = getConfig().getConfigurationSection("recipes").getKeys(false);

        for (String recipeKey : recipeKeys) {
            String base = "recipes." + recipeKey + ".";

            String iconName = getConfig().getString(base + "icon", "MUSHROOM_STEW").toUpperCase();
            Material iconMat;
            try {
                iconMat = Material.valueOf(iconName);
            } catch (IllegalArgumentException ex) {
                getLogger().warning(
                        "Invalid material '" + iconName + "' for recipe " + recipeKey + " — using MUSHROOM_STEW.");
                iconMat = Material.MUSHROOM_STEW;
            }

            ItemStack item = new ItemStack(iconMat);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(getConfig().getString(base + "name", recipeKey));
            List<String> lore = getConfig().getStringList(base + "lore");
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }

            meta.getPersistentDataContainer().set(
                    new NamespacedKey(this, "featurebox_" + recipeKey + "_id"),
                    PersistentDataType.INTEGER,
                    1);

            boolean glow = getConfig().getBoolean(base + "glow", true);
            if (glow) {
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);

            NamespacedKey recipeNsKey = new NamespacedKey(this, "featurebox_" + recipeKey);
            ShapedRecipe recipe;
            try {
                recipe = new ShapedRecipe(recipeNsKey, item);
            } catch (Exception ex) {
                getLogger().warning("Unable to create recipe for " + recipeKey + " : " + ex.getMessage());
                continue;
            }

            List<String> shape = getConfig().getStringList(base + "shape");
            if (shape == null || shape.isEmpty() || shape.size() > 3) {
                getLogger().warning("Invalid shape for recipe " + recipeKey + " — must be 1..3 lines.");
                continue;
            }
            List<String> normalized = new ArrayList<>();
            for (String line : shape) {
                if (line == null || line.isEmpty()) {
                    continue;
                }
                String norm = line.replace('/', ' ');
                if (norm.length() > 3) {
                    getLogger().warning("Shape line longer than 3 characters for " + recipeKey + ", truncating.");
                    norm = norm.substring(0, 3);
                }
                normalized.add(norm);
            }

            if (normalized.size() == 1) {
                recipe.shape(normalized.get(0));
            } else if (normalized.size() == 2) {
                recipe.shape(normalized.get(0), normalized.get(1));
            } else {
                recipe.shape(normalized.get(0), normalized.get(1), normalized.get(2));
            }

            List<Map<?, ?>> ingredients = getConfig().getMapList(base + "ingredients");
            if (ingredients != null) {
                for (Map<?, ?> ing : ingredients) {
                    Object shortcutObj = ing.get("shortcut");
                    Object keyObj = ing.get("key");
                    if (shortcutObj == null || keyObj == null) {
                        getLogger().warning("Malformed ingredient for " + recipeKey + " (missing shortcut or key).");
                        continue;
                    }
                    char ch = shortcutObj.toString().charAt(0);
                    String matName = keyObj.toString().toUpperCase();
                    Material mat;
                    try {
                        mat = Material.valueOf(matName);
                    } catch (IllegalArgumentException ex) {
                        getLogger().warning("Invalid ingredient material '" + matName + "' for " + recipeKey);
                        continue;
                    }
                    recipe.setIngredient(ch, mat);
                }
            }

            Bukkit.getServer().addRecipe(recipe);
            getLogger().info("Registered recipe: " + recipeKey);
        }
    }
}
