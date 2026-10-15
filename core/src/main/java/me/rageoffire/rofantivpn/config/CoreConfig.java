package me.rageoffire.rofantivpn.config;

import java.util.Collections;
import java.util.Set;

/**
 * Platform-agnostic config data holder.
 * Populated by platform-specific loaders (SpigotConfigManager, ProxyConfigLoader).
 * Used by core utilities like DiscordUtil and VpnLogger.
 */
public class CoreConfig {

    // API
    protected String apiKey        = "";
    protected String fallbackApi   = "ipquery";
    protected String apiFailAction = "allow";

    // Cache
    protected long cacheTtlMinutes = 1440L;

    // Detection
    protected int     minRisk    = 50;
    protected boolean checkVpn   = true;
    protected boolean checkProxy = true;
    protected boolean checkTor   = true;

    // Whitelist
    protected Set<String> whitelistIps       = Collections.emptySet();
    protected Set<String> whitelistCountries = Collections.emptySet();

    // Punishment
    protected String punishmentAction  = "kick";
    protected String punishmentCommand = "ban %player% Using VPN/Proxy";

    // Logs
    protected boolean logConsole = true;
    protected boolean logPlayer  = true;
    protected boolean logFile    = true;

    // Messages
    protected String kickMessage    = "\u00A7cSorry, VPNs are not allowed on this server!";
    protected String alertMessage   = "\u00A7e[AntiVPN] \u00A7f%player% \u00A77(%ip%) \u00A7e\u2014 Risk: \u00A7f%risk%% \u00A7e| Country: \u00A7f%country% \u00A7e| Type: \u00A7f%type%";
    protected String bypassedMessage = "\u00A77[AntiVPN] \u00A7f%player% \u00A77has bypass permission, skipping check.";

    // Discord
    protected boolean discordEnabled     = false;
    protected String  discordWebhookUrl  = "";
    protected String  discordName        = "ROF_AntiVPN";
    protected String  discordAvatar      = "";
    protected String  discordTitle       = "VPN/Proxy Detected";
    protected String  discordDescription = "%player% joined the server using a VPN/Proxy";
    protected int     discordColor       = 15158332;
    protected String  discordThumbnail   = "";

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getApiKey()        { return apiKey; }
    public String getFallbackApi()   { return fallbackApi; }
    public String getApiFailAction() { return apiFailAction; }

    public long    getCacheTtlMinutes() { return cacheTtlMinutes; }
    public int     getMinRisk()         { return minRisk; }
    public boolean isCheckVpn()         { return checkVpn; }
    public boolean isCheckProxy()       { return checkProxy; }
    public boolean isCheckTor()         { return checkTor; }

    public Set<String> getWhitelistIps()       { return whitelistIps; }
    public Set<String> getWhitelistCountries() { return whitelistCountries; }

    public String getPunishmentAction()  { return punishmentAction; }
    public String getPunishmentCommand() { return punishmentCommand; }

    public boolean isLogConsole() { return logConsole; }
    public boolean isLogPlayer()  { return logPlayer; }
    public boolean isLogFile()    { return logFile; }

    public String getKickMessage()     { return kickMessage; }
    public String getAlertMessage()    { return alertMessage; }
    public String getBypassedMessage() { return bypassedMessage; }

    public boolean isDiscordEnabled()     { return discordEnabled; }
    public String  getDiscordWebhookUrl() { return discordWebhookUrl; }
    public String  getDiscordName()       { return discordName; }
    public String  getDiscordAvatar()     { return discordAvatar; }
    public String  getDiscordTitle()      { return discordTitle; }
    public String  getDiscordDescription(){ return discordDescription; }
    public int     getDiscordColor()      { return discordColor; }
    public String  getDiscordThumbnail()  { return discordThumbnail; }
}
