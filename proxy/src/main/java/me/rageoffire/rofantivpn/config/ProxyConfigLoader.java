package me.rageoffire.rofantivpn.config;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Loads CoreConfig from config.yml for both BungeeCord and Velocity.
 * Uses SnakeYAML (already bundled in BungeeCord/Velocity) — no Bukkit dependency.
 */
public class ProxyConfigLoader extends CoreConfig {

    private final File dataFolder;
    private final Logger logger;
    private final Class<?> pluginClass; // used to load default resource

    public ProxyConfigLoader(File dataFolder, Logger logger, Class<?> pluginClass) {
        this.dataFolder  = dataFolder;
        this.logger      = logger;
        this.pluginClass = pluginClass;
        reload();
    }

    @SuppressWarnings("unchecked")
    public void reload() {
        if (!dataFolder.exists()) dataFolder.mkdirs();
        File cfgFile = new File(dataFolder, "config.yml");

        // Save default config.yml if absent
        if (!cfgFile.exists()) {
            try (InputStream in = pluginClass.getResourceAsStream("/config.yml")) {
                if (in != null) Files.copy(in, cfgFile.toPath());
            } catch (IOException e) {
                logger.warning("[AntiVPN] Could not save default config: " + e.getMessage());
            }
        }

        Map<String, Object> cfg;
        try (FileInputStream fis = new FileInputStream(cfgFile)) {
            cfg = new Yaml().load(fis);
        } catch (IOException e) {
            logger.severe("[AntiVPN] Failed to load config.yml: " + e.getMessage());
            cfg = Collections.emptyMap();
        }

        // API
        apiKey        = str(cfg, "api-key", "").trim();
        fallbackApi   = str(cfg, "fallback-api", "ipquery").trim().toLowerCase();
        apiFailAction = str(cfg, "api-fail-action", "allow").trim().toLowerCase();

        // Cache
        Map<String, Object> cacheMap = section(cfg, "cache");
        cacheTtlMinutes = num(cacheMap, "ttl", 1440L);

        // Detection
        Map<String, Object> det = section(cfg, "detection");
        minRisk    = (int) num(det, "min-risk", 50L);
        checkVpn   = bool(det, "check-vpn",   true);
        checkProxy = bool(det, "check-proxy",  true);
        checkTor   = bool(det, "check-tor",    true);

        // Whitelist
        Map<String, Object> wl = section(cfg, "whitelist");
        whitelistIps = new HashSet<>(list(wl, "ips"));
        whitelistCountries = new HashSet<>();
        for (String c : list(wl, "countries")) whitelistCountries.add(c.toUpperCase());

        // Punishment
        Map<String, Object> pun = section(cfg, "punishment");
        punishmentAction  = str(pun, "action",  "kick").trim().toLowerCase();
        punishmentCommand = str(pun, "command",  "ban %player% Using VPN/Proxy");

        // Logs
        Map<String, Object> logs = section(cfg, "logs");
        logConsole = bool(logs, "console", true);
        logPlayer  = bool(logs, "player",  true);
        logFile    = bool(logs, "file",    true);

        // Messages
        Map<String, Object> msgs = section(cfg, "messages");
        kickMessage     = color(str(msgs, "kick-message",
                "&cSorry, VPNs are not allowed on this server!"));
        alertMessage    = color(str(msgs, "alert-message",
                "&e[AntiVPN] &f%player% &7(%ip%) &e| Risk: &f%risk%% &e| Country: &f%country% &e| Type: &f%type%"));
        bypassedMessage = color(str(msgs, "bypassed",
                "&7[AntiVPN] &f%player% &7has bypass permission, skipping check."));

        // Discord
        Map<String, Object> disc = section(cfg, "discord-webhook");
        discordEnabled     = bool(disc, "enable",      false);
        discordWebhookUrl  = str(disc,  "webhook-url", "");
        discordName        = str(disc,  "name",        "ROF_AntiVPN");
        discordAvatar      = str(disc,  "avatar",      "");
        discordTitle       = str(disc,  "title",       "VPN/Proxy Detected");
        discordDescription = str(disc,  "description", "%player% joined using a VPN/Proxy");
        discordColor       = (int) num(disc, "color",  15158332L);
        discordThumbnail   = str(disc,  "thumbnail",   "");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> section(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return (v instanceof Map) ? (Map<String, Object>) v : Collections.emptyMap();
    }

    private String str(Map<String, Object> map, String key, String def) {
        Object v = map.get(key);
        return (v instanceof String s) ? s : def;
    }

    private long num(Map<String, Object> map, String key, long def) {
        Object v = map.get(key);
        return (v instanceof Number n) ? n.longValue() : def;
    }

    private boolean bool(Map<String, Object> map, String key, boolean def) {
        Object v = map.get(key);
        return (v instanceof Boolean b) ? b : def;
    }

    @SuppressWarnings("unchecked")
    private List<String> list(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return (v instanceof List) ? (List<String>) v : Collections.emptyList();
    }

    private String color(String s) {
        return s == null ? "" : s.replace("&", "\u00A7");
    }
}
