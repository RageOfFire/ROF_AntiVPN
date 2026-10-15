package me.rageoffire.rofantivpn.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;

/**
 * Spigot/Paper implementation of CoreConfig.
 * Loads all values from Bukkit's FileConfiguration (config.yml).
 */
public class SpigotConfigManager extends CoreConfig {

    private final JavaPlugin plugin;
    private FileConfiguration cfg;

    public SpigotConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        cfg = plugin.getConfig();

        // API
        apiKey        = cfg.getString("api-key", "").trim();
        fallbackApi   = cfg.getString("fallback-api", "ipquery").trim().toLowerCase();
        apiFailAction = cfg.getString("api-fail-action", "allow").trim().toLowerCase();

        // Cache
        cacheTtlMinutes = cfg.getLong("cache.ttl", 1440L);

        // Detection
        minRisk    = cfg.getInt("detection.min-risk", 50);
        checkVpn   = cfg.getBoolean("detection.check-vpn",   true);
        checkProxy = cfg.getBoolean("detection.check-proxy", true);
        checkTor   = cfg.getBoolean("detection.check-tor",   true);

        // Whitelist
        whitelistIps = new HashSet<>(cfg.getStringList("whitelist.ips"));
        List<String> countries = cfg.getStringList("whitelist.countries");
        whitelistCountries = new HashSet<>();
        for (String c : countries) whitelistCountries.add(c.toUpperCase());

        // Punishment
        punishmentAction  = cfg.getString("punishment.action", "kick").trim().toLowerCase();
        punishmentCommand = cfg.getString("punishment.command", "ban %player% Using VPN/Proxy");

        // Logs
        logConsole = cfg.getBoolean("logs.console", true);
        logPlayer  = cfg.getBoolean("logs.player",  true);
        logFile    = cfg.getBoolean("logs.file",     true);

        // Messages (translate & color codes)
        kickMessage     = color(cfg.getString("messages.kick-message",
                "&cSorry, VPNs are not allowed on this server!"));
        alertMessage    = color(cfg.getString("messages.alert-message",
                "&e[AntiVPN] &f%player% &7(%ip%) &e| Risk: &f%risk%% &e| Country: &f%country% &e| Type: &f%type%"));
        bypassedMessage = color(cfg.getString("messages.bypassed",
                "&7[AntiVPN] &f%player% &7has bypass permission, skipping check."));

        // Discord
        discordEnabled     = cfg.getBoolean("discord-webhook.enable", false);
        discordWebhookUrl  = cfg.getString("discord-webhook.webhook-url", "");
        discordName        = cfg.getString("discord-webhook.name",        "ROF_AntiVPN");
        discordAvatar      = cfg.getString("discord-webhook.avatar",      "");
        discordTitle       = cfg.getString("discord-webhook.title",       "VPN/Proxy Detected");
        discordDescription = cfg.getString("discord-webhook.description", "%player% joined using a VPN/Proxy");
        discordColor       = cfg.getInt("discord-webhook.color",          15158332);
        discordThumbnail   = cfg.getString("discord-webhook.thumbnail",   "");
    }

    /** Save the api-key to config.yml on disk. */
    public void saveApiKey(String key) {
        this.apiKey = key;
        cfg.set("api-key", key);
        plugin.saveConfig();
    }

    /** Quick accessor for config messages with & color code support. */
    public String getMessage(String key) {
        return color(cfg.getString("messages." + key,
                "&c[AntiVPN] Message not found: " + key));
    }

    private String color(String s) {
        return s == null ? "" : s.replace("&", "\u00A7");
    }
}
