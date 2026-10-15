package me.rageoffire.rofantivpn.manager;

import me.rageoffire.rofantivpn.ROFAntiVPN;
import me.rageoffire.rofantivpn.api.IpQueryProvider;
import me.rageoffire.rofantivpn.api.ProxyCheckProvider;
import me.rageoffire.rofantivpn.api.VpnCheckResult;
import me.rageoffire.rofantivpn.api.VpnProvider;
import me.rageoffire.rofantivpn.cache.IpCache;
import me.rageoffire.rofantivpn.config.SpigotSpigotConfigManager;
import me.rageoffire.rofantivpn.log.VpnLogger;
import me.rageoffire.rofantivpn.util.DiscordUtil;
import me.rageoffire.rofantivpn.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates the full VPN-check lifecycle for a joining player:
 * whitelist → cache → API chain → punishment → logging/alerts.
 *
 * All API calls happen on an async thread; kicks happen on the main thread.
 */
public class VpnCheckManager {

    private final ROFAntiVPN plugin;
    private final SpigotConfigManager config;
    private final VpnLogger logger;
    private final IpCache cache;

    private List<VpnProvider> providers;

    public VpnCheckManager(ROFAntiVPN plugin, SpigotConfigManager config,
                           VpnLogger logger, IpCache cache) {
        this.plugin = plugin;
        this.config = config;
        this.logger = logger;
        this.cache  = cache;
        buildProviders();
    }

    /** Rebuild provider chain after a reload. */
    public void buildProviders() {
        providers = new ArrayList<>();
        providers.add(new ProxyCheckProvider(config.getApiKey()));

        if (config.getFallbackApi().equals("ipquery")) {
            providers.add(new IpQueryProvider());
        }
    }

    /**
     * Entry point called from the PreLogin listener.
     * Runs asynchronously; schedules punishment on the main thread if needed.
     *
     * @param player the joining player (already on server, not yet fully in)
     * @param ip     the raw IP string (without port)
     */
    public void checkAsync(Player player, String ip) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // 1. Bypass permission
            if (player.hasPermission("rofvpn.bypass")) {
                if (config.isLogConsole()) {
                    logger.logInfo(MessageUtil.formatPlayer(
                            config.getMessage("bypassed"), player.getName()));
                }
                return;
            }

            // 2. IP whitelist
            if (config.getWhitelistIps().contains(ip)) {
                logger.logInfo(player.getName() + " (" + ip + ") is whitelisted by IP.");
                return;
            }

            // 3. Cache lookup
            Optional<VpnCheckResult> cached = cache.get(ip);
            if (cached.isPresent()) {
                logger.logInfo("Cache hit for " + ip + " (" + player.getName() + ")");
                handleResult(player, ip, cached.get());
                return;
            }

            // 4. Walk provider chain
            VpnCheckResult result = null;
            for (VpnProvider provider : providers) {
                Optional<VpnCheckResult> opt = provider.check(ip);
                if (opt.isPresent()) {
                    result = opt.get();
                    break;
                }
                logger.logWarning("Provider " + provider.getName()
                        + " failed for " + ip + " — trying next.");
            }

            // 5. All providers failed
            if (result == null) {
                logger.logWarning("All providers failed for " + ip
                        + " (" + player.getName() + "). Action: " + config.getApiFailAction());
                if (config.getApiFailAction().equals("deny")) {
                    punish(player, ip, new VpnCheckResult(true, -1, "N/A", "API Failure", "none"));
                }
                return;
            }

            // 6. Cache the successful result
            cache.put(ip, result);

            // 7. Country whitelist check
            if (!result.getCountry().equals("N/A")
                    && config.getWhitelistCountries().contains(result.getCountry().toUpperCase())) {
                logger.logInfo(player.getName() + " (" + ip + ") country "
                        + result.getCountry() + " is whitelisted.");
                return;
            }

            // 8. Evaluate detection against config thresholds
            handleResult(player, ip, result);
        });
    }

    /**
     * Evaluate a result and apply punishment + logging if warranted.
     * Still on async thread here; punishment is dispatched to main thread.
     */
    private void handleResult(Player player, String ip, VpnCheckResult result) {
        if (!shouldPunish(result)) return;

        // Log + alert
        logger.logDetection(player.getName(), ip, result);
        sendPlayerAlerts(player, ip, result);
        DiscordUtil.sendAlert(config, player.getName(), ip, result);

        // Kick on main thread
        punish(player, ip, result);
    }

    /**
     * Decide whether detection+risk threshold should cause a punishment.
     */
    private boolean shouldPunish(VpnCheckResult result) {
        if (!result.isVpnDetected()) return false;

        // If we have a real risk score, apply the threshold
        if (result.getRiskScore() >= 0) {
            return result.getRiskScore() >= config.getMinRisk();
        }

        // No risk score available (ipquery doesn't always return one) — trust the boolean flags
        return true;
    }

    private void punish(Player player, String ip, VpnCheckResult result) {
        String action = config.getPunishmentAction();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;

            if (action.equals("kick")) {
                String kickMsg = MessageUtil.format(
                        config.getMessage("kick-message"), player.getName(), ip, result);
                player.kickPlayer(kickMsg);

            } else if (action.equals("command")) {
                String cmd = MessageUtil.format(
                        config.getPunishmentCommand(), player.getName(), ip, result);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        });
    }

    private void sendPlayerAlerts(Player detected, String ip, VpnCheckResult result) {
        if (!config.isLogPlayer()) return;
        String alertMsg = MessageUtil.format(
                config.getMessage("alert-message"),
                detected.getName(), ip, result);

        Bukkit.getScheduler().runTask(plugin, () ->
            Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("rofvpn.alert"))
                .forEach(p -> p.sendMessage(alertMsg))
        );
    }

    public IpCache getCache() { return cache; }
}
