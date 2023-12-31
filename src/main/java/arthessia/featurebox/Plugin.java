package arthessia.featurebox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;

import arthessia.featurebox.commands.CommonCommands.ReloadCommand;
import arthessia.featurebox.commands.RiptideCommands.RiptideEnabled;
import arthessia.featurebox.commands.RiptideCommands.RiptideForce;
import arthessia.featurebox.commands.UnusedCommands.UnusedChance;
import arthessia.featurebox.commands.UnusedCommands.UnusedLimit;
import arthessia.featurebox.commands.UnusedCommands.UnusedSpawn;
import arthessia.featurebox.objects.Data;
import arthessia.featurebox.ondeath.OnDeath;
import arthessia.featurebox.ondeath.OnDeathCommands.OnDeathToggle;
import arthessia.featurebox.riptide.Riptide;
import arthessia.featurebox.unused.Unused;

public class Plugin extends JavaPlugin implements Listener {
    private static File DATAD = new File("plugins/featurebox/data.json");
    private static Data DATA = new Data();
    public static final Random RANDOM = new Random();

    @Override
    public void onEnable() {
        getLogger().info("Featurebox loading...");
        this.saveDefaultConfig();
        getLogger().info("Config loaded... ");

        if(!this.getConfig().contains("unused.zombiehorse.spawn.limit")) {
            this.getConfig().set("unused.zombiehorse.spawn.limit", 3);
            this.saveConfig();
            getLogger().info("Setup of new default values...");
        }
        if(!this.getConfig().contains("common.location.death.enabled")) {
            this.getConfig().set("common.location.death.enabled", true);
            this.saveConfig();
            getLogger().info("Setup of new default values...");
        }

        this.getCommand("featurebox").setExecutor(new ReloadCommand(this));
        this.getCommand("unusedtoggle").setExecutor(new UnusedSpawn(this));
        this.getCommand("unusedchance").setExecutor(new UnusedChance(this));
        this.getCommand("unusedlimit").setExecutor(new UnusedLimit(this));
        this.getCommand("riptidetoggle").setExecutor(new RiptideEnabled(this));
        this.getCommand("riptideforce").setExecutor(new RiptideForce(this));
        this.getCommand("ondeathtoggle").setExecutor(new OnDeathToggle(this));
        getLogger().info("Commands loaded...");
        
        Riptide riptide = new Riptide(this);
        Unused unused = new Unused(this);
        OnDeath onDeath = new OnDeath(this);
        Bukkit.getServer().getPluginManager().registerEvents(onDeath, this);
        Bukkit.getServer().getPluginManager().registerEvents(riptide, this);
        Bukkit.getServer().getPluginManager().registerEvents(unused, this);
        getLogger().info("Features loaded...");
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
}
