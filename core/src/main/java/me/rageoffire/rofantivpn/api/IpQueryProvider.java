package me.rageoffire.rofantivpn.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.rageoffire.rofantivpn.util.HttpUtil;

import java.util.Optional;

/**
 * Provider backed by ipquery.io — completely free, no API key, no registration.
 * Docs: https://ipquery.io/
 *
 * Response shape for GET https://api.ipquery.io/{ip}:
 * {
 *   "ip": "1.2.3.4",
 *   "isp": { "org": "...", "isp": "...", "asn": "..." },
 *   "location": {
 *     "country": "United States",
 *     "country_code": "US",
 *     "city": "...",
 *     "state": "...",
 *     "timezone": "..."
 *   },
 *   "risk": {
 *     "is_mobile": false,
 *     "is_vpn": true,
 *     "is_tor": false,
 *     "is_proxy": true,
 *     "is_datacenter": false,
 *     "risk_score": 75
 *   }
 * }
 */
public class IpQueryProvider implements VpnProvider {

    private static final String BASE = "https://api.ipquery.io/%s";

    @Override
    public String getName() { return "ipquery.io (fallback)"; }

    @Override
    public Optional<VpnCheckResult> check(String ip) {
        Optional<String> body = HttpUtil.get(String.format(BASE, ip));
        if (body.isEmpty()) return Optional.empty();

        try {
            JsonObject root = JsonParser.parseString(body.get()).getAsJsonObject();

            // Extract country
            String country = "N/A";
            if (root.has("location")) {
                JsonObject loc = root.getAsJsonObject("location");
                if (loc.has("country_code")) country = loc.get("country_code").getAsString();
            }

            // Extract risk fields
            boolean isVpn       = false;
            boolean isProxy     = false;
            boolean isTor       = false;
            boolean isDatacenter = false;
            int riskScore       = -1;

            if (root.has("risk")) {
                JsonObject risk = root.getAsJsonObject("risk");
                isVpn        = risk.has("is_vpn")        && risk.get("is_vpn").getAsBoolean();
                isProxy      = risk.has("is_proxy")      && risk.get("is_proxy").getAsBoolean();
                isTor        = risk.has("is_tor")        && risk.get("is_tor").getAsBoolean();
                isDatacenter = risk.has("is_datacenter") && risk.get("is_datacenter").getAsBoolean();
                riskScore    = risk.has("risk_score")    ? risk.get("risk_score").getAsInt() : -1;
            }

            boolean detected = isVpn || isProxy || isTor;

            // Build a human-readable type string
            String type = buildType(isVpn, isProxy, isTor, isDatacenter);

            return Optional.of(new VpnCheckResult(detected, riskScore, country, type, getName()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String buildType(boolean vpn, boolean proxy, boolean tor, boolean dc) {
        if (!vpn && !proxy && !tor && !dc) return "Clean";
        StringBuilder sb = new StringBuilder();
        if (vpn)   sb.append("VPN");
        if (proxy) { if (sb.length() > 0) sb.append("/"); sb.append("Proxy"); }
        if (tor)   { if (sb.length() > 0) sb.append("/"); sb.append("Tor"); }
        if (dc)    { if (sb.length() > 0) sb.append("/"); sb.append("Datacenter"); }
        return sb.toString();
    }
}
