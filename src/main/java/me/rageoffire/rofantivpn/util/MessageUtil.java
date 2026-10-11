package me.rageoffire.rofantivpn.util;

import me.rageoffire.rofantivpn.api.VpnCheckResult;

/**
 * Utility for replacing placeholders inside message strings.
 * Placeholders: %player%, %ip%, %risk%, %country%, %type%
 */
public final class MessageUtil {

    private MessageUtil() {}

    /**
     * Replace all VPN-related placeholders in a message template.
     */
    public static String format(String template,
                                String player,
                                String ip,
                                VpnCheckResult result) {
        return template
                .replace("%player%",  player  == null ? "" : player)
                .replace("%ip%",      ip      == null ? "" : ip)
                .replace("%risk%",    result  == null ? "N/A" : result.getRiskDisplay())
                .replace("%country%", result  == null ? "N/A" : result.getCountry())
                .replace("%type%",    result  == null ? "N/A" : result.getType());
    }

    /**
     * Replace only %player% (for messages that don't relate to a result).
     */
    public static String formatPlayer(String template, String player) {
        return template.replace("%player%", player == null ? "" : player);
    }
}
