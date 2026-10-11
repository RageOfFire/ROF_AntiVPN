package me.rageoffire.rofantivpn.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.rageoffire.rofantivpn.api.IpQueryProvider;
import me.rageoffire.rofantivpn.api.ProxyCheckProvider;
import me.rageoffire.rofantivpn.api.VpnCheckResult;
import me.rageoffire.rofantivpn.api.VpnProvider;
import me.rageoffire.rofantivpn.cache.IpCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Velocity entry point.
 * Deploy this jar to your Velocity plugins/ folder.
 *
 * NOTE: The Velocity API is not included in this Maven project by default.
 * To compile the Velocity module, add the Velocity API dependency to pom.xml:
 *
 *   <dependency>
 *     <groupId>com.velocitypowered</groupId>
 *     <artifactId>velocity-api</artifactId>
 *     <version>3.3.0-SNAPSHOT</version>
 *     <scope>provided</scope>
 *   </dependency>
 *
 * and the PaperMC repository:
 *   https://repo.papermc.io/repository/maven-public/
 */
@Plugin(
    id = "rof-antivpn",
    name = "ROF_AntiVPN",
    version = "3.0.0",
    description = "Detect players using VPN/Proxy — Velocity edition",
    authors = {"RageOfFire"}
)
public class ROFAntiVPNVelocity {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private final List<VpnProvider> providers = new ArrayList<>();
    private final IpCache cache = new IpCache(1440L);

    private int    minRisk;
    private String apiFailAction;
    private String kickMessage;
    private Set<String> whitelistIps      = new HashSet<>();
    private Set<String> whitelistCountries = new HashSet<>();

    @Inject
    public ROFAntiVPNVelocity(ProxyServer server, Logger logger,
                               @DataDirectory Path dataDirectory) {
        this.server        = server;
        this.logger        = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        loadConfig();
        server.getEventManager().register(this, this);
        logger.info("ROF_AntiVPN (Velocity) 3.0.0 enabled.");
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();

        Optional<java.net.InetSocketAddress> addrOpt = player.getRemoteAddress() instanceof java.net.InetSocketAddress a
                ? Optional.of(a) : Optional.empty();
        if (addrOpt.isEmpty()) return;

        String ip = addrOpt.get().getAddress().getHostAddress();

        // Skip local
        if (ip.equals("127.0.0.1") || ip.startsWith("192.168.")
                || ip.startsWith("10.") || ip.equals("0:0:0:0:0:0:0:1")) return;

        // Bypass
        if (player.hasPermission("rofvpn.bypass")) return;

        // IP whitelist
        if (whitelistIps.contains(ip)) return;

        server.getScheduler().buildTask(this, () -> {
            Optional<VpnCheckResult> cached = cache.get(ip);
            VpnCheckResult result;

            if (cached.isPresent()) {
                result = cached.get();
            } else {
                result = null;
                for (VpnProvider provider : providers) {
                    Optional<VpnCheckResult> opt = provider.check(ip);
                    if (opt.isPresent()) {
                        result = opt.get();
                        cache.put(ip, result);
                        break;
                    }
                }
            }

            if (result == null) {
                if (apiFailAction.equals("deny")) disconnect(player);
                return;
            }

            // Country whitelist
            if (!result.getCountry().equals("N/A")
                    && whitelistCountries.contains(result.getCountry())) return;

            if (shouldPunish(result)) {
                logger.warn("[AntiVPN] DETECTED {} ({}) Risk:{} Country:{} Type:{}",
                        player.getUsername(), ip,
                        result.getRiskDisplay(), result.getCountry(), result.getType());
                disconnect(player);
            }
        }).schedule();
    }

    private void disconnect(Player player) {
        Component msg = LegacyComponentSerializer.legacyAmpersand().deserialize(kickMessage);
        player.disconnect(msg);
    }

    private boolean shouldPunish(VpnCheckResult r) {
        if (!r.isVpnDetected()) return false;
        if (r.getRiskScore() >= 0) return r.getRiskScore() >= minRisk;
        return true;
    }

    @SuppressWarnings("unchecked")
    private void loadConfig() {
        File dataDir = dataDirectory.toFile();
        if (!dataDir.exists()) dataDir.mkdirs();
        File cfgFile = new File(dataDir, "config.yml");

        if (!cfgFile.exists()) {
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                if (in != null) Files.copy(in, cfgFile.toPath());
            } catch (IOException e) {
                logger.warn("Could not save default config: {}", e.getMessage());
            }
        }

        Map<String, Object> cfg;
        try (FileInputStream fis = new FileInputStream(cfgFile)) {
            cfg = new Yaml().load(fis);
        } catch (IOException e) {
            logger.error("Failed to load config.yml: {}", e.getMessage());
            cfg = Map.of();
        }

        String apiKey = (String) cfg.getOrDefault("api-key", "");
        String fallback = ((String) cfg.getOrDefault("fallback-api", "ipquery")).toLowerCase();
        apiFailAction = (String) cfg.getOrDefault("api-fail-action", "allow");

        Map<String, Object> detection = (Map<String, Object>) cfg.getOrDefault("detection", Map.of());
        minRisk = (int) detection.getOrDefault("min-risk", 50);

        Map<String, Object> messages = (Map<String, Object>) cfg.getOrDefault("messages", Map.of());
        kickMessage = (String) messages.getOrDefault("kick-message",
                "&cVPNs are not allowed on this server!");

        Map<String, Object> whitelist = (Map<String, Object>) cfg.getOrDefault("whitelist", Map.of());
        whitelistIps = new HashSet<>((List<String>) whitelist.getOrDefault("ips", List.of()));
        List<String> countries = (List<String>) whitelist.getOrDefault("countries", List.of());
        for (String c : countries) whitelistCountries.add(c.toUpperCase());

        Map<String, Object> cacheMap = (Map<String, Object>) cfg.getOrDefault("cache", Map.of());
        long ttl = ((Number) cacheMap.getOrDefault("ttl", 1440)).longValue();
        cache.setTtlMinutes(ttl);

        providers.clear();
        providers.add(new ProxyCheckProvider(apiKey));
        if (fallback.equals("ipquery")) providers.add(new IpQueryProvider());
    }
}
