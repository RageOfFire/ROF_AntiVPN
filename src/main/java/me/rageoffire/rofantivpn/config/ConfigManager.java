package me.rageoffire.rofantivpn.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads and exposes all configuration values from config.yml.
 * Re-instantiate (or call reload()) after /rofvpn reload.
 */
public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration cfg;

    // API
    private String apiKey;
    private String fallbackApi;
    private String apiFailAction;

    // Cache
    private long cacheTtlMinutes;

    // Detection
    private int minRisk;
    private boolean checkVpn;
    private boolean checkProxy;
    private boolean checkTor;

    // Whitelist
    private Set<String> whitelistIps;
    private Set<String> whitelistCountries;

    // Punishment
    private String punishmentAction;
    private String punishmentCommand;

    // Logs
    private boolean logConsole;
    private boolean logPlayer;
    private boolean logFile;

    // Discord
    private boolean discordEnabled;
    private String discordWebhookUrl;
    private String discordName;
    private String discordAvatar;
    private String discordTitle;
    private String discordDescription;
    private int discordColor;
    private String discordThumbnail;

    public ConfigManager(JavaPlugin plugin) {
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
        List<String> ips = cfg.getStringList("whitelist.ips");
        whitelistIps = new HashSet<>(ips);

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

        // Discord
        discordEnabled    = cfg.getBoolean("discord-webhook.enable", false);
        discordWebhookUrl = cfg.getString("discord-webhook.webhook-url", "");
        discordName       = cfg.getString("discord-webhook.name",    "ROF_AntiVPN");
        discordAvatar     = cfg.getString("discord-webhook.avatar",  "");
        discordTitle      = cfg.getString("discord-webhook.title",   "VPN/Proxy Detected");
        discordDescription = cfg.getString("discord-webhook.description", "%player% joined using a VPN/Proxy");
        discordColor      = cfg.getInt("discord-webhook.color",      15158332);
        discordThumbnail  = cfg.getString("discord-webhook.thumbnail", "");
    }

    /** Save the api-key to config.yml on disk. */
    public void saveApiKey(String key) {
        this.apiKey = key;
        cfg.set("api-key", key);
        plugin.saveConfig();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getApiKey()        { return apiKey; }
    public String getFallbackApi()   { return fallbackApi; }
    public String getApiFailAction() { return apiFailAction; }

    public long getCacheTtlMinutes() { return cacheTtlMinutes; }

    public int  getMinRisk()    { return minRisk; }
    public boolean isCheckVpn() { return checkVpn; }
    public boolean isCheckProxy(){ return checkProxy; }
    public boolean isCheckTor() { return checkTor; }

    public Set<String> getWhitelistIps()       { return Collections.unmodifiableSet(whitelistIps); }
    public Set<String> getWhitelistCountries() { return Collections.unmodifiableSet(whitelistCountries); }

    public String getPunishmentAction()  { return punishmentAction; }
    public String getPunishmentCommand() { return punishmentCommand; }

    public boolean isLogConsole() { return logConsole; }
    public boolean isLogPlayer()  { return logPlayer; }
    public boolean isLogFile()    { return logFile; }

    public boolean isDiscordEnabled()     { return discordEnabled; }
    public String getDiscordWebhookUrl()  { return discordWebhookUrl; }
    public String getDiscordName()        { return discordName; }
    public String getDiscordAvatar()      { return discordAvatar; }
    public String getDiscordTitle()       { return discordTitle; }
    public String getDiscordDescription() { return discordDescription; }
    public int    getDiscordColor()       { return discordColor; }
    public String getDiscordThumbnail()   { return discordThumbnail; }

    /** Quick accessor for config messages with & color code support. */
    public String getMessage(String key) {
        return cfg.getString("messages." + key, "&c[AntiVPN] Message not found: " + key)
                  .replace("&", "\u00A7");
    }
}
