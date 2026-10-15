package me.rageoffire.rofantivpn.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Utility to detect local / private / reserved IP addresses that should
 * never be sent to an external VPN-detection API.
 *
 * Covers:
 *  - IPv4 loopback        127.0.0.0/8
 *  - IPv4 private         10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16
 *  - IPv4 link-local      169.254.0.0/16
 *  - IPv4 shared space    100.64.0.0/10  (RFC 6598 — carrier-grade NAT)
 *  - IPv6 loopback        ::1
 *  - IPv6 link-local      fe80::/10
 *  - IPv6 unique-local    fc00::/7
 *  - IPv6 site-local      fec0::/10  (deprecated but still seen)
 *  - Unspecified          0.0.0.0 / ::
 */
public final class LocalAddressUtil {

    private LocalAddressUtil() {}

    /**
     * Returns true if the given IP string is a local / private / reserved address
     * that should be skipped without any API call.
     *
     * @param ip raw IP string (IPv4 or IPv6, no port)
     * @return true if the address is local/private
     */
    public static boolean isLocal(String ip) {
        if (ip == null || ip.isBlank()) return true;

        // Fast string-based checks (avoids InetAddress parsing overhead for common cases)
        if (ip.equals("127.0.0.1"))           return true;  // IPv4 loopback
        if (ip.equals("0.0.0.0"))             return true;  // unspecified
        if (ip.equals("::1"))                 return true;  // IPv6 loopback
        if (ip.equals("::"))                  return true;  // IPv6 unspecified
        if (ip.equals("0:0:0:0:0:0:0:1"))    return true;  // IPv6 loopback (expanded)
        if (ip.equals("0:0:0:0:0:0:0:0"))    return true;  // IPv6 unspecified (expanded)

        if (ip.startsWith("10."))             return true;  // 10.0.0.0/8
        if (ip.startsWith("192.168."))        return true;  // 192.168.0.0/16
        if (ip.startsWith("127."))            return true;  // 127.0.0.0/8 loopback range
        if (ip.startsWith("169.254."))        return true;  // link-local
        if (ip.startsWith("fe80:") || ip.startsWith("FE80:")) return true; // IPv6 link-local
        if (ip.startsWith("fc") || ip.startsWith("fd") ||
            ip.startsWith("FC") || ip.startsWith("FD")) return true; // IPv6 unique-local fc00::/7

        // 172.16.0.0/12  →  172.16.x.x – 172.31.x.x
        if (ip.startsWith("172.")) {
            try {
                int second = Integer.parseInt(ip.split("\\.")[1]);
                if (second >= 16 && second <= 31) return true;
            } catch (NumberFormatException ignored) {}
        }

        // 100.64.0.0/10  →  100.64.x.x – 100.127.x.x  (carrier-grade NAT)
        if (ip.startsWith("100.")) {
            try {
                int second = Integer.parseInt(ip.split("\\.")[1]);
                if (second >= 64 && second <= 127) return true;
            } catch (NumberFormatException ignored) {}
        }

        // fec0::/10 (deprecated IPv6 site-local)
        if (ip.length() >= 4) {
            String prefix = ip.substring(0, 4).toLowerCase();
            if (prefix.equals("fec0") || prefix.equals("fed0") ||
                prefix.equals("fee0") || prefix.equals("fef0")) return true;
        }

        // Final fallback: let the JDK decide via InetAddress
        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.isLoopbackAddress()
                || addr.isSiteLocalAddress()
                || addr.isLinkLocalAddress()
                || addr.isAnyLocalAddress()
                || addr.isMulticastAddress();
        } catch (UnknownHostException e) {
            // Unparseable address — skip it safely
            return true;
        }
    }
}
