package me.rageoffire.rofantivpn.api;

import java.util.Optional;

/**
 * Contract that every VPN-detection API provider must implement.
 * All implementations must be non-blocking safe (called from async thread).
 */
public interface VpnProvider {

    /**
     * Name of this provider, used in logs and alerts.
     */
    String getName();

    /**
     * Check the given IP address.
     *
     * @param ip IPv4 or IPv6 address string
     * @return Optional containing the result, or empty on failure / rate-limit
     */
    Optional<VpnCheckResult> check(String ip);
}
