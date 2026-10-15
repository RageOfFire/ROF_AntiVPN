package me.rageoffire.rofantivpn.cache;

import me.rageoffire.rofantivpn.api.VpnCheckResult;

/**
 * A single IP → result entry stored in the in-memory cache.
 */
public class CacheEntry {

    private final VpnCheckResult result;
    private final long expiresAt;   // System.currentTimeMillis()

    public CacheEntry(VpnCheckResult result, long ttlMillis) {
        this.result    = result;
        this.expiresAt = System.currentTimeMillis() + ttlMillis;
    }

    public VpnCheckResult getResult() { return result; }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}
