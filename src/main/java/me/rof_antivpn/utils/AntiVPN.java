package me.rof_antivpn.utils;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import me.rof_antivpn.ROF_AntiVPN;

import java.util.logging.Level;

import static org.bukkit.Bukkit.getLogger;


public class AntiVPN {

    public static String isVPN(String ipAddress) {
        try {
            String url = "https://proxycheck.io/v2/" + ipAddress + "?key=" + ROF_AntiVPN.proxiesAPIKey + "&vpn=1&asn=1&risk=1&port=1&seen=1&days=7&tag=ROF-AntiVPN";

            HttpResponse<JsonNode> response = Unirest.get(url)
                    .header("User-Agent", "Mozilla/5.0")
                    .asJson();

            int responseCode = response.getStatus();
            if (responseCode == 200) {
                kong.unirest.json.JSONObject jsonObject = response.getBody().getObject();

                if (jsonObject.has(ipAddress)) {
                    kong.unirest.json.JSONObject ipObject = jsonObject.getJSONObject(ipAddress);
                    if (ipObject.has("proxy")) {
                        return ipObject.getString("proxy");
                    }
                }
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Failed to get proxy value for " + ipAddress, e);
        }
        return null;
    }
}
