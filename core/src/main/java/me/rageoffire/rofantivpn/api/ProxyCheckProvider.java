package me.rageoffire.rofantivpn.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.rageoffire.rofantivpn.util.HttpUtil;

import java.util.Optional;

/**
 * Provider backed by proxycheck.io.
 * Free plan: 1 000 queries/day (no key), 1 000/day per key on free tier.
 * Docs: https://proxycheck.io/api/
 *
 * Response shape (vpn=1&risk=1&node=1):
 * {
 *   "status": "ok",
 *   "1.2.3.4": {
 *     "proxy": "yes"|"no",
 *     "type": "VPN"|"Proxy"|"Tor"|…,
 *     "risk": 75,
 *     "country": "US",
 *     "isocode": "US"
 *   }
 * }
 */
public class ProxyCheckProvider implements VpnProvider {

    private static final String BASE = "https://proxycheck.io/v2/%s?vpn=1&risk=1&node=1&asn=1";
    private static final String KEY_PARAM = "&key=%s";

    private final String apiKey;

    public ProxyCheckProvider(String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    @Override
    public String getName() { return "proxycheck.io"; }

    @Override
    public Optional<VpnCheckResult> check(String ip) {
        String url = String.format(BASE, ip);
        if (!apiKey.isEmpty()) url += String.format(KEY_PARAM, apiKey);

        Optional<String> body = HttpUtil.get(url);
        if (body.isEmpty()) return Optional.empty();

        try {
            JsonObject root = JsonParser.parseString(body.get()).getAsJsonObject();

            // If status is not ok, bail
            if (!root.has("status") || !root.get("status").getAsString().equalsIgnoreCase("ok")) {
                return Optional.empty();
            }

            // The IP field holds the actual result
            if (!root.has(ip)) return Optional.empty();
            JsonObject data = root.getAsJsonObject(ip);

            boolean proxy  = data.has("proxy") && data.get("proxy").getAsString().equalsIgnoreCase("yes");
            int risk       = data.has("risk") ? data.get("risk").getAsInt() : -1;
            String country = data.has("isocode") ? data.get("isocode").getAsString() : "N/A";
            String type    = data.has("type") ? data.get("type").getAsString() : (proxy ? "Proxy" : "Clean");

            return Optional.of(new VpnCheckResult(proxy, risk, country, type, getName()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
