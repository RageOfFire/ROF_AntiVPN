package me.rageoffire.rofantivpn.util;

import me.rageoffire.rofantivpn.api.VpnCheckResult;
import me.rageoffire.rofantivpn.config.CoreConfig;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Sends a Discord webhook embed on VPN detection.
 * Runs synchronously — always call from an async thread.
 */
public final class DiscordUtil {

    private DiscordUtil() {}

    public static void sendAlert(CoreConfig config,
                                 String playerName,
                                 String ip,
                                 VpnCheckResult result) {
        if (!config.isDiscordEnabled()) return;
        String webhookUrl = config.getDiscordWebhookUrl();
        if (webhookUrl.isBlank()) return;

        String description = MessageUtil.format(config.getDiscordDescription(), playerName, ip, result);
        String thumbnail   = MessageUtil.formatPlayer(config.getDiscordThumbnail(), playerName);

        String payload = buildPayload(
                config.getDiscordName(),
                config.getDiscordAvatar(),
                config.getDiscordTitle(),
                description,
                config.getDiscordColor(),
                thumbnail,
                playerName, ip, result
        );

        try {
            HttpURLConnection conn = (HttpURLConnection)
                    URI.create(webhookUrl).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "ROF_AntiVPN/3.0");
            conn.setConnectTimeout(5_000);
            conn.setReadTimeout(8_000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }
            conn.getResponseCode();
        } catch (Exception ignored) {}
    }

    private static String buildPayload(String username, String avatarUrl,
                                       String title, String description,
                                       int color, String thumbnail,
                                       String player, String ip,
                                       VpnCheckResult result) {
        return "{"
             + "\"username\":"   + jsonStr(username) + ","
             + "\"avatar_url\":" + jsonStr(avatarUrl) + ","
             + "\"embeds\":[{"
             +   "\"title\":"       + jsonStr(title) + ","
             +   "\"description\":" + jsonStr(description) + ","
             +   "\"color\":"       + color + ","
             +   "\"thumbnail\":{\"url\":" + jsonStr(thumbnail) + "},"
             +   "\"fields\":["
             +     field("Player",  player,                        true) + ","
             +     field("IP",      ip,                            true) + ","
             +     field("Risk",    result.getRiskDisplay() + "%", true) + ","
             +     field("Country", result.getCountry(),           true) + ","
             +     field("Type",    result.getType(),              true)
             +   "]"
             + "}]}";
    }

    private static String field(String name, String value, boolean inline) {
        return "{\"name\":" + jsonStr(name)
             + ",\"value\":" + jsonStr(value)
             + ",\"inline\":" + inline + "}";
    }

    private static String jsonStr(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
