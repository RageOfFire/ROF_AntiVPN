package me.rageoffire.rofantivpn;

import me.rageoffire.rofantivpn.cache.IpCache;
import me.rageoffire.rofantivpn.command.VpnCommand;
import me.rageoffire.rofantivpn.config.ConfigManager;
import me.rageoffire.rofantivpn.listener.PlayerJoinListener;
import me.rageoffire.rofantivpn.log.VpnLogger;
import me.rageoffire.rofantivpn.manager.PlaceholderAPIHook;
import me.rageoffire.rofantivpn.manager.VpnCheckManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ROFAntiVPN extends JavaPlugin {

    private ConfigManager   configManager;
    private VpnLogger       vpnLogger;
    private IpCache         ipCache;
    private VpnCheckManager checkManager;

    @Override
    public void onEnable() {
        // 1. Config
        configManager = new ConfigManager(this);

        // 2. Logger
        vpnLogger = new VpnLogger(
                getDataFolder(),
                getLogger(),
                configManager.isLogFile(),
                configManager.isLogConsole()
        );

        // 3. Cache
        ipCache = new IpCache(configManager.getCacheTtlMinutes());

        // 4. Check manager
        checkManager = new VpnCheckManager(this, configManager, vpnLogger, ipCache);

        // 5. Events
        Bukkit.getPluginManager().registerEvents(
                new PlayerJoinListener(checkManager), this);

        // 6. Commands
        var cmd = getCommand("rofvpn");
        if (cmd != null) {
            var handler = new VpnCommand(this, configManager, checkManager);
            cmd.setExecutor(handler);
        }

        // 7. PlaceholderAPI (soft depend)
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this, ipCache).register();
            vpnLogger.logInfo("PlaceholderAPI hooked successfully.");
        }

        vpnLogger.logInfo("ROF_AntiVPN v" + getDescription().getVersion() + " enabled.");

        if (configManager.getApiKey().isBlank()) {
            vpnLogger.logWarning(configManager.getMessage("setup-key")
                    .replace("\u00A7", ""));
        }
    }

    @Override
    public void onDisable() {
        if (vpnLogger != null) {
            vpnLogger.logInfo("ROF_AntiVPN disabled.");
        }
    }

    // Accessors for BungeeCord bridge if needed
    public ConfigManager   getConfigManager()  { return configManager; }
    public VpnCheckManager getCheckManager()   { return checkManager; }
    public VpnLogger       getVpnLogger()      { return vpnLogger; }
    public IpCache         getIpCache()        { return ipCache; }
}
