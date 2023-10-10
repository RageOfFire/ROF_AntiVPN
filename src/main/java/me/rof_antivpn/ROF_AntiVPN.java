package me.rof_antivpn;

import com.tchristofferson.configupdater.ConfigUpdater;
import me.rof_antivpn.commands.VPNCommandExecutor;
import me.rof_antivpn.commands.VPNTabCompletion;
import me.rof_antivpn.events.PlayerLoginEventListener;
import me.rof_antivpn.utils.FileLog;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;

public final class ROF_AntiVPN extends JavaPlugin {
    public static String proxiesAPIKey;
    private FileLog fileLog;

    @Override
    public void onEnable() {
        // Plugin startup logic
        fileLog = new FileLog(this);
        // bstats
        int pluginId = 19187;
        new Metrics(this, pluginId);

        //Setup Config
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        File configFile = new File(getDataFolder(), "config.yml");

        try {
            ConfigUpdater.update(this, "config.yml", configFile, Arrays.asList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadConfiguration();
        reloadConfig();
        // Listen
        getServer().getPluginManager().registerEvents(new PlayerLoginEventListener(this), this);
        // Command
        getCommand("rofvpn").setExecutor(new VPNCommandExecutor(this));
        getCommand("rofvpn").setTabCompleter(new VPNTabCompletion());
    }

    public void loadConfiguration() {
        FileConfiguration config = getConfig();
        proxiesAPIKey = config.getString("APIKey");
        String setupmess = config.getString("messages.setup-key");
        if (Objects.equals(proxiesAPIKey, "your-api-key") || proxiesAPIKey == null) {
            getLogger().log(Level.WARNING, setupmess);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
