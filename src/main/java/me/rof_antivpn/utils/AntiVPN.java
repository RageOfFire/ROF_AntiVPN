package me.rof_antivpn.utils;

import com.google.gson.Gson;
import me.rof_antivpn.ROF_AntiVPN;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getLogger;


public class AntiVPN {
    private static final Gson gson = new Gson();

    public static JsonObject isVPN(String ipAddress) {
        try {
            String url = "https://proxycheck.io/v2/" + ipAddress + "?key=" + ROF_AntiVPN.proxiesAPIKey + "&vpn=1&asn=1&risk=1&port=1&seen=1&days=7&tag=ROF-AntiVPN";

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {
                    String responseBody = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                    // Parse the JSON response
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                    if (jsonResponse.has(ipAddress)) {
                        JsonObject ipData = jsonResponse.getAsJsonObject(ipAddress);
                        return ipData;
                    }
                    connection.disconnect();
                } catch (Exception e) {
                    getLogger().log(Level.WARNING, "An error occurred while checking the IP address: " + ipAddress, e);
                }
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Failed to get proxy value for " + ipAddress, e);
        }
        return null;
    }
}
