package me.rageoffire.rofantivpn.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Minimal HTTP GET helper. Intentionally has zero external dependencies
 * beyond the JDK so it works in both Spigot and BungeeCord environments.
 */
public final class HttpUtil {

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS    = 8_000;

    private HttpUtil() {}

    /**
     * Performs a blocking HTTP GET and returns the response body as a String.
     * Returns {@link Optional#empty()} on any network or HTTP error.
     *
     * @param url full URL string (must be HTTPS or HTTP)
     * @return response body or empty
     */
    public static Optional<String> get(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", "ROF_AntiVPN/3.0 (+https://github.com/RageOfFire/ROF_AntiVPN)");
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();
            if (code != 200) return Optional.empty();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String body = reader.lines().collect(Collectors.joining());
                return body.isBlank() ? Optional.empty() : Optional.of(body);
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
