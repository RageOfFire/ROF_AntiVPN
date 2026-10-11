package me.rageoffire.rofantivpn.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * BungeeCord entry point.
 * Deploy this jar to your BungeeCord's plugins/ folder.
 * It reads the same config.yml format as the Spigot edition.
 */
public class ROFAntiVPNBungee extends Plugin {

    private Configuration config;

    @Override
    public void onEnable() {
        // Save default config.yml
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        File cfgFile = new File(getDataFolder(), "config.yml");
        if (!cfgFile.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                if (in != null) Files.copy(in, cfgFile.toPath());
            } catch (IOException e) {
                getLogger().warning("Could not save default config: " + e.getMessage());
            }
        }

        // Load config
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(cfgFile);
        } catch (IOException e) {
            getLogger().severe("Failed to load config.yml: " + e.getMessage());
            return;
        }

        // Register listener
        BungeeCheckManager manager = new BungeeCheckManager(this);
        getProxy().getPluginManager().registerListener(this, manager);

        getLogger().info("ROF_AntiVPN (BungeeCord) v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ROF_AntiVPN (BungeeCord) disabled.");
    }

    public Configuration getConfig() { return config; }
}
