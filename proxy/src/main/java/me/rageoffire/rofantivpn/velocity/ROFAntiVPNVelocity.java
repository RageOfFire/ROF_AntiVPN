package me.rageoffire.rofantivpn.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.rageoffire.rofantivpn.api.IpQueryProvider;
import me.rageoffire.rofantivpn.api.ProxyCheckProvider;
import me.rageoffire.rofantivpn.api.VpnCheckResult;
import me.rageoffire.rofantivpn.api.VpnProvider;
import me.rageoffire.rofantivpn.cache.IpCache;
import me.rageoffire.rofantivpn.config.ProxyConfigLoader;
import me.rageoffire.rofantivpn.util.DiscordUtil;
import me.rageoffire.rofantivpn.util.LocalAddressUtil;
import me.rageoffire.rofantivpn.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Plugin(
    id = "rof-antivpn",
    name = "ROF_AntiVPN",
    version = "3.0.0",
    description = "Detect players using VPN/Proxy — Velocity edition",
    authors = {"RageOfFire"}
)
public class ROFAntiVPNVelocity {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private ProxyConfigLoader config;
    private IpCache cache;
    private final List<VpnProvider> providers = new ArrayList<>();

    @Inject
    public ROFAntiVPNVelocity(ProxyServer server, Logger logger,
                               @DataDirectory Path dataDirectory) {
        this.server        = server;
        this.logger        = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        config = new ProxyConfigLoader(dataDirectory.toFile(), toJulLogger(), getClass());
        cache  = new IpCache(config.getCacheTtlMinutes());

        buildProviders();
        server.getEventManager().register(this, this);

        logger.info("ROF_AntiVPN (Velocity) 3.0.0 enabled.");
        if (config.getApiKey().isBlank()) {
            logger.warn("[AntiVPN] No API key set — using ipquery.io fallback only.");
        }
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();

        if (!(player.getRemoteAddress() instanceof InetSocketAddress addr)) return;
        String ip = addr.getAddress().getHostAddress();

        if (LocalAddressUtil.isLocal(ip)) return;
        if (player.hasPermission("rofvpn.bypass")) return;
        if (config.getWhitelistIps().contains(ip)) return;

        server.getScheduler().buildTask(this, () -> {
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
                    logger.warn("[AntiVPN] Provider {} failed for {} — trying next.", provider.getName(), ip);
                }
            }

            if (result == null) {
                logger.warn("[AntiVPN] All providers failed for {}. Action: {}", ip, config.getApiFailAction());
                if (config.getApiFailAction().equals("deny")) disconnect(player, config.getKickMessage());
                return;
            }

            // Country whitelist
            if (!result.getCountry().equals("N/A")
                    && config.getWhitelistCountries().contains(result.getCountry().toUpperCase())) return;

            if (!shouldPunish(result)) return;

            logger.warn("[AntiVPN] DETECTED {} ({}) Risk:{} Country:{} Type:{}",
                    player.getUsername(), ip,
                    result.getRiskDisplay(), result.getCountry(), result.getType());

            // Alert online staff
            String alertMsg = MessageUtil.format(config.getAlertMessage(), player.getUsername(), ip, result);
            Component alertComponent = LegacyComponentSerializer.legacySection().deserialize(alertMsg);
            server.getAllPlayers().stream()
                    .filter(p -> p.hasPermission("rofvpn.alert"))
                    .forEach(p -> p.sendMessage(alertComponent));

            // Discord
            DiscordUtil.sendAlert(config, player.getUsername(), ip, result);

            // Disconnect
            disconnect(player, MessageUtil.format(config.getKickMessage(), player.getUsername(), ip, result));
        }).schedule();
    }

    private void disconnect(Player player, String msg) {
        player.disconnect(LegacyComponentSerializer.legacySection().deserialize(msg));
    }

    private boolean shouldPunish(VpnCheckResult result) {
        if (!result.isVpnDetected()) return false;
        if (result.getRiskScore() >= 0) return result.getRiskScore() >= config.getMinRisk();
        return true;
    }

    private void buildProviders() {
        providers.clear();
        providers.add(new ProxyCheckProvider(config.getApiKey()));
        if (config.getFallbackApi().equals("ipquery")) providers.add(new IpQueryProvider());
    }

    /** Adapts SLF4J logger to java.util.logging for ProxyConfigLoader. */
    private java.util.logging.Logger toJulLogger() {
        return java.util.logging.Logger.getLogger("ROF_AntiVPN");
    }
}
