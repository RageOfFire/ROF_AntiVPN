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

        String param = params.toLowerCase();
        if ("detected".equals(param)) {
            return opt.isPresent() ? String.valueOf(opt.get().isVpnDetected()) : "unknown";
        } else if ("risk".equals(param)) {
            return opt.isPresent() ? opt.get().getRiskDisplay() : "N/A";
        } else if ("country".equals(param)) {
            return opt.isPresent() ? opt.get().getCountry() : "N/A";
        } else if ("type".equals(param)) {
            return opt.isPresent() ? opt.get().getType() : "N/A";
        } else if ("provider".equals(param)) {
            return opt.isPresent() ? opt.get().getProvider() : "N/A";
        } else if ("cached".equals(param)) {
            return String.valueOf(opt.isPresent());
        } else {
            return "";
        }
    }
}
