package me.rof_antivpn;

import me.rof_antivpn.commands.VPNCommandExecutor;
import me.rof_antivpn.commands.VPNTabCompletion;
import me.rof_antivpn.events.PlayerLoginEventListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public final class ROF_AntiVPN extends JavaPlugin {
    public static String proxiesAPIKey;

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("ROF_AntiVPN has starting...");

        //Setup Config
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        loadConfiguration();
        // Listen
        getServer().getPluginManager().registerEvents(new PlayerLoginEventListener(this), this);
        // Command
        getCommand("rofvpn").setExecutor(new VPNCommandExecutor(this));
        getCommand("rofvpn").setTabCompleter(new VPNTabCompletion());
    }


    public void loadConfiguration() {
        FileConfiguration config = getConfig();
        proxiesAPIKey = config.getString("APIKey");
        String setupmess = config.getString("setup-key");
        if (Objects.equals(proxiesAPIKey, "your-api-key") || Objects.equals(proxiesAPIKey, "") || proxiesAPIKey == null) {
            getLogger().log(Level.WARNING, setupmess);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("ROF_AntiVPN has stopping...");
    }
}