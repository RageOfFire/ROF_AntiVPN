package me.rageoffire.rofantivpn.cache;

import me.rageoffire.rofantivpn.api.VpnCheckResult;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory cache for IP check results.
 * Expired entries are lazily removed on every get/put.
 */
public class IpCache {

    private final Map<String, CacheEntry> store = new ConcurrentHashMap<>();
    private long ttlMillis;

    public IpCache(long ttlMinutes) {
        this.ttlMillis = ttlMinutes * 60_000L;
    }

    /** Store a result for the given IP. */
    public void put(String ip, VpnCheckResult result) {
        evictExpired();
        store.put(ip, new CacheEntry(result, ttlMillis));
    }

    /**
     * Retrieve a non-expired result for the given IP.
     * Returns empty if not cached or expired.
     */
    public Optional<VpnCheckResult> get(String ip) {
        CacheEntry entry = store.get(ip);
        if (entry == null) return Optional.empty();
        if (entry.isExpired()) {
            store.remove(ip);
            return Optional.empty();
        }
        return Optional.of(entry.getResult());
    }

    /** Remove all entries regardless of expiry. Returns count removed. */
    public int clear() {
        int count = store.size();
        store.clear();
        return count;
    }

    /** How many IPs are currently cached (including potentially expired). */
    public int size() { return store.size(); }

    /** Update TTL (minutes) without clearing the cache. */
    public void setTtlMinutes(long minutes) {
        this.ttlMillis = minutes * 60_000L;
    }

    /** Remove all expired entries proactively. */
    private void evictExpired() {
        store.entrySet().removeIf(e -> e.getValue().isExpired());
    }
}
