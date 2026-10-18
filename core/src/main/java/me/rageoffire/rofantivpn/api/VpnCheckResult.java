package me.rageoffire.rofantivpn.api;

/**
 * Unified result returned by every API provider.
 */
public class VpnCheckResult {

    private final boolean vpnDetected;
    private final int riskScore;     // 0-100; -1 if unknown
    private final String country;    // ISO 3166-1 alpha-2 code, or "N/A"
    private final String type;       // "VPN", "Proxy", "Tor", "Clean", etc.
    private final String provider;   // which API returned this result

    public VpnCheckResult(boolean vpnDetected, int riskScore, String country, String type, String provider) {
        this.vpnDetected = vpnDetected;
        this.riskScore   = riskScore;
        this.country     = country == null || country.trim().isEmpty() ? "N/A" : country;
        this.type        = type == null || type.trim().isEmpty() ? "Unknown" : type;
        this.provider    = provider;
    }

    /** True when the IP was flagged as VPN, proxy, or Tor. */
    public boolean isVpnDetected() { return vpnDetected; }

    /** 0–100 risk score. Returns -1 when the provider does not supply one. */
    public int getRiskScore() { return riskScore; }

    public String getCountry()  { return country; }
    public String getType()     { return type; }
    public String getProvider() { return provider; }

    /** Returns riskScore as a display string, or "N/A" if unknown. */
    public String getRiskDisplay() {
        return riskScore >= 0 ? String.valueOf(riskScore) : "N/A";
    }
}
