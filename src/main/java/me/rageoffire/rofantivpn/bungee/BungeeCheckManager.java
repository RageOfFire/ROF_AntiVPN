package me.rageoffire.rofantivpn.bungee;

import me.rageoffire.rofantivpn.api.IpQueryProvider;
import me.rageoffire.rofantivpn.api.ProxyCheckProvider;
import me.rageoffire.rofantivpn.api.VpnCheckResult;
import me.rageoffire.rofantivpn.api.VpnProvider;
import me.rageoffire.rofantivpn.cache.IpCache;
import me.rageoffire.rofantivpn.util.DiscordUtil;
import me.rageoffire.rofantivpn.util.LocalAddressUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * BungeeCord listener that performs async VPN checks at the proxy level,
 * before the player ever reaches a backend server.
 */
public class BungeeCheckManager implements Listener {

    private final ROFAntiVPNBungee plugin;
    private final IpCache cache;
    private final List<VpnProvider> providers = new ArrayList<>();

    // Quick-access config mirrors (BungeeCord has its own config API)
    private final String apiKey;
    private final String fallbackApi;
    private final String apiFailAction;
    private final int    minRisk;
    private final String kickMessage;
    private final Set<String> whitelistIps;
    private final Set<String> whitelistCountries;
    private final boolean logEnabled;

    public BungeeCheckManager(ROFAntiVPNBungee plugin) {
        this.plugin = plugin;

        net.md_5.bungee.config.Configuration cfg = plugin.getConfig();

        apiKey          = cfg.getString("api-key", "").trim();
        fallbackApi     = cfg.getString("fallback-api", "ipquery").trim().toLowerCase();
        apiFailAction   = cfg.getString("api-fail-action", "allow");
        minRisk         = cfg.getInt("detection.min-risk", 50);
        kickMessage     = ChatColor.translateAlternateColorCodes('&',
                cfg.getString("messages.kick-message",
                        "&cVPNs are not allowed on this server!"));
        logEnabled      = cfg.getBoolean("logs.console", true);

        whitelistIps = new HashSet<>(cfg.getStringList("whitelist.ips"));
        whitelistCountries = new HashSet<>();
        for (String c : cfg.getStringList("whitelist.countries")) {
            whitelistCountries.add(c.toUpperCase());
        }

        cache = new IpCache(cfg.getLong("cache.ttl", 1440L));

        // Build provider chain
        providers.add(new ProxyCheckProvider(apiKey));
        if (fallbackApi.equals("ipquery")) providers.add(new IpQueryProvider());
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        InetSocketAddress addr = (InetSocketAddress) player.getSocketAddress();
        if (addr == null) return;
        String ip = addr.getAddress().getHostAddress();

        // Skip loopback, private, link-local, and all other reserved ranges
        if (LocalAddressUtil.isLocal(ip)) return;

        // Bypass permission
        if (player.hasPermission("rofvpn.bypass")) return;

        // IP whitelist
        if (whitelistIps.contains(ip)) return;

        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            // Cache lookup
            Optional<VpnCheckResult> cached = cache.get(ip);
            VpnCheckResult result;

            if (cached.isPresent()) {
                result = cached.get();
            } else {
                result = null;
                for (VpnProvider provider : providers) {
                    Optional<VpnCheckResult> opt = provider.check(ip);
                    if (opt.isPresent()) {
                        result = opt.get();
                        cache.put(ip, result);
                        break;
                    }
                    log("Provider " + provider.getName() + " failed for " + ip + " — trying next.");
                }
            }

            // All providers failed
            if (result == null) {
                log("All providers failed for " + ip + ". Action: " + apiFailAction);
                if (apiFailAction.equals("deny")) {
                    disconnect(player, kickMessage);
                }
                return;
            }

            // Country whitelist
            if (!result.getCountry().equals("N/A")
                    && whitelistCountries.contains(result.getCountry().toUpperCase())) {
                return;
            }

            // Evaluate
            if (shouldPunish(result)) {
                log("[DETECTION] " + player.getName() + " (" + ip + ")"
                        + " Risk:" + result.getRiskDisplay()
                        + " Country:" + result.getCountry()
                        + " Type:" + result.getType()
                        + " Provider:" + result.getProvider());
                disconnect(player, kickMessage);
            }
        });
    }

    private void disconnect(ProxiedPlayer player, String msg) {
        plugin.getProxy().getScheduler().schedule(plugin,
                () -> player.disconnect(net.md_5.bungee.api.chat.TextComponent.fromLegacyText(msg)),
                50, TimeUnit.MILLISECONDS);
    }

    private boolean shouldPunish(VpnCheckResult result) {
        if (!result.isVpnDetected()) return false;
        if (result.getRiskScore() >= 0) return result.getRiskScore() >= minRisk;
        return true;
    }

    private void log(String msg) {
        if (logEnabled) plugin.getLogger().warning("[AntiVPN] " + msg);
    }
}
