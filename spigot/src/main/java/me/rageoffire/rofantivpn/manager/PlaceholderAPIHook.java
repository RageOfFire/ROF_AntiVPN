package me.rageoffire.rofantivpn.manager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.rageoffire.rofantivpn.ROFAntiVPN;
import me.rageoffire.rofantivpn.api.VpnCheckResult;
import me.rageoffire.rofantivpn.cache.IpCache;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Registers %rofvpn_*% placeholders with PlaceholderAPI.
 *
 * Available placeholders:
 *   %rofvpn_detected%   → "true" / "false"
 *   %rofvpn_risk%       → "75" / "N/A"
 *   %rofvpn_country%    → "US"
 *   %rofvpn_type%       → "VPN" / "Proxy" / "Clean"
 *   %rofvpn_provider%   → which API returned the result
 *   %rofvpn_cached%     → "true" if result is in cache
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final ROFAntiVPN plugin;
    private final IpCache    cache;

    public PlaceholderAPIHook(ROFAntiVPN plugin, IpCache cache) {
        this.plugin = plugin;
        this.cache  = cache;
    }

    @Override public @NotNull String getIdentifier() { return "rofvpn"; }
    @Override public @NotNull String getAuthor()     { return "RageOfFire"; }
    @Override public @NotNull String getVersion()    { return plugin.getDescription().getVersion(); }
    @Override public boolean persist()               { return true; }
    @Override public boolean canRegister()           { return true; }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        String ip = player.getAddress() != null
                ? player.getAddress().getAddress().getHostAddress()
                : null;

        if (ip == null) return "";

        Optional<VpnCheckResult> opt = cache.get(ip);

        return switch (params.toLowerCase()) {
            case "detected"  -> opt.map(r -> String.valueOf(r.isVpnDetected())).orElse("unknown");
            case "risk"      -> opt.map(VpnCheckResult::getRiskDisplay).orElse("N/A");
            case "country"   -> opt.map(VpnCheckResult::getCountry).orElse("N/A");
            case "type"      -> opt.map(VpnCheckResult::getType).orElse("N/A");
            case "provider"  -> opt.map(VpnCheckResult::getProvider).orElse("N/A");
            case "cached"    -> String.valueOf(opt.isPresent());
            default          -> "";
        };
    }
}
