package me.rageoffire.rofantivpn.bungee;

import me.rageoffire.rofantivpn.api.IpQueryProvider;
import me.rageoffire.rofantivpn.api.ProxyCheckProvider;
import me.rageoffire.rofantivpn.api.VpnCheckResult;
import me.rageoffire.rofantivpn.api.VpnProvider;
import me.rageoffire.rofantivpn.cache.IpCache;
import me.rageoffire.rofantivpn.config.CoreConfig;
import me.rageoffire.rofantivpn.util.DiscordUtil;
import me.rageoffire.rofantivpn.util.LocalAddressUtil;
import me.rageoffire.rofantivpn.util.MessageUtil;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * BungeeCord listener — performs async VPN checks at the proxy level.
 */
public class BungeeCheckManager implements Listener {

    private final ROFAntiVPNBungee plugin;
    private final CoreConfig config;
    private final IpCache cache;
    private final List<VpnProvider> providers = new ArrayList<>();

    public BungeeCheckManager(ROFAntiVPNBungee plugin, CoreConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.cache  = new IpCache(config.getCacheTtlMinutes());
        buildProviders();
    }

    private void buildProviders() {
        providers.clear();
        providers.add(new ProxyCheckProvider(config.getApiKey()));
        if (config.getFallbackApi().equals("ipquery")) {
            providers.add(new IpQueryProvider());
        }
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        InetSocketAddress addr = (InetSocketAddress) player.getSocketAddress();
        if (addr == null) return;
        String ip = addr.getAddress().getHostAddress();

        if (LocalAddressUtil.isLocal(ip)) return;
        if (player.hasPermission("rofvpn.bypass")) return;
        if (config.getWhitelistIps().contains(ip)) return;

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

            if (result == null) {
                log("All providers failed for " + ip + ". Action: " + config.getApiFailAction());
                if (config.getApiFailAction().equals("deny")) disconnect(player, config.getKickMessage());
                return;
            }

            // Country whitelist
            if (!result.getCountry().equals("N/A")
                    && config.getWhitelistCountries().contains(result.getCountry().toUpperCase())) return;

            if (!shouldPunish(result)) return;

            // Log
            log("[DETECTION] " + player.getName() + " (" + ip + ")"
                    + " Risk:" + result.getRiskDisplay()
                    + " Country:" + result.getCountry()
                    + " Type:" + result.getType()
                    + " Provider:" + result.getProvider());

            // Alert online staff
            String alertMsg = MessageUtil.format(config.getAlertMessage(), player.getName(), ip, result);
            plugin.getProxy().getPlayers().stream()
                    .filter(p -> p.hasPermission("rofvpn.alert"))
                    .forEach(p -> p.sendMessage(TextComponent.fromLegacyText(alertMsg)));

            // Discord
            DiscordUtil.sendAlert(config, player.getName(), ip, result);

            // Disconnect
            String kickMsg = MessageUtil.format(config.getKickMessage(), player.getName(), ip, result);
            disconnect(player, kickMsg);
        });
    }

    private void disconnect(ProxiedPlayer player, String msg) {
        plugin.getProxy().getScheduler().schedule(plugin,
                () -> player.disconnect(TextComponent.fromLegacyText(msg)),
                50, TimeUnit.MILLISECONDS);
    }

    private boolean shouldPunish(VpnCheckResult result) {
        if (!result.isVpnDetected()) return false;
        if (result.getRiskScore() >= 0) return result.getRiskScore() >= config.getMinRisk();
        return true;
    }

    private void log(String msg) {
        if (config.isLogConsole()) plugin.getLogger().warning("[AntiVPN] " + msg);
    }
}
