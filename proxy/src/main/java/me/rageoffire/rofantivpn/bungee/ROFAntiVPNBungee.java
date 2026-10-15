package me.rageoffire.rofantivpn.bungee;

import me.rageoffire.rofantivpn.config.ProxyConfigLoader;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * BungeeCord entry point for ROF_AntiVPN.
 * Drop ROF_AntiVPN-proxy.jar into your BungeeCord plugins/ folder.
 */
public class ROFAntiVPNBungee extends Plugin {

    private ProxyConfigLoader config;

    @Override
    public void onEnable() {
        config = new ProxyConfigLoader(getDataFolder(), getLogger(), getClass());

        BungeeCheckManager manager = new BungeeCheckManager(this, config);
        getProxy().getPluginManager().registerListener(this, manager);

        getLogger().info("ROF_AntiVPN (BungeeCord) v" + getDescription().getVersion() + " enabled.");

        if (config.getApiKey().isBlank()) {
            getLogger().warning("[AntiVPN] No API key set — using ipquery.io fallback only.");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("ROF_AntiVPN (BungeeCord) disabled.");
    }

    public ProxyConfigLoader getConfig() { return config; }
}
