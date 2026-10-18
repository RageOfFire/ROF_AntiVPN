<div align="center">

# 🛡️ ROF_AntiVPN

**Detect and block players joining with VPN, Proxy, or Tor — for Spigot, Paper, BungeeCord and Velocity.**

[![Downloads](https://img.shields.io/github/downloads/RageOfFire/ROF_AntiVPN/total?style=for-the-badge&color=brightgreen)](https://github.com/RageOfFire/ROF_AntiVPN/releases)
[![Release](https://img.shields.io/github/v/release/RageOfFire/ROF_AntiVPN?style=for-the-badge&color=blue)](https://github.com/RageOfFire/ROF_AntiVPN/releases/latest)
[![License](https://img.shields.io/github/license/RageOfFire/ROF_AntiVPN?style=for-the-badge)](LICENSE)
[![Discord](https://img.shields.io/discord/752171524919918672?style=for-the-badge&color=5865F2&label=Discord)](https://discord.com/invite/FSUsV3jZ6G)
[![Stars](https://img.shields.io/github/stars/RageOfFire/ROF_AntiVPN?style=for-the-badge&color=yellow)](https://github.com/RageOfFire/ROF_AntiVPN/stargazers)
[![Commit Activity](https://img.shields.io/github/commit-activity/m/RageOfFire/ROF_AntiVPN?style=for-the-badge)](https://github.com/RageOfFire/ROF_AntiVPN/commits/master)

</div>

---

## ✨ Features

- 🔍 **Dual-API detection** — [proxycheck.io](https://proxycheck.io) as primary, [ipquery.io](https://ipquery.io) as free automatic fallback (no key needed)
- ⚡ **IP result caching** — results cached for 24 hours (configurable), cutting API usage and speeding up logins
- 🔒 **Fully async** — all API calls run off the main thread, zero TPS impact
- 📊 **Risk score threshold** — only punish players above a configurable risk score (0–100)
- 🌍 **Country & IP whitelist** — allow specific IPs or entire countries
- 🔔 **Discord webhook alerts** — rich embeds with player info, risk score, country and type
- 💬 **PlaceholderAPI support** — expose `%rofvpn_*%` placeholders for scoreboards and chat
- 📁 **Daily log files** — timestamped detection logs in `plugins/ROF_AntiVPN/logs/`
- 🌐 **Universal** — one jar for backend servers (Spigot/Paper), separate jar for proxy (BungeeCord + Velocity)
- ☕ **Java 8 compatible** — runs on Minecraft 1.8 and above

---

## 📦 Downloads

| Platform | Download |
|----------|----------|
| **Spigot / Paper** (backend) | [SpigotMC](https://www.spigotmc.org/resources/rof_antivpn.111408/) · [GitHub Releases](https://github.com/RageOfFire/ROF_AntiVPN/releases/latest) |
| **BungeeCord / Velocity** (proxy) | [GitHub Releases](https://github.com/RageOfFire/ROF_AntiVPN/releases/latest) |

> ⚠️ If you run a proxy network, install the **proxy jar** on BungeeCord/Velocity only. Do **not** install both jars on the same server.

---

## 🚀 Quick Start

1. Download the correct jar for your platform from [Releases](https://github.com/RageOfFire/ROF_AntiVPN/releases/latest)
2. Drop it into your `plugins/` folder
3. Start the server — `config.yml` is generated automatically
4. *(Optional)* Add a free API key from [proxycheck.io](https://proxycheck.io) for higher limits:
   ```
   /rofvpn setapikey YOUR_KEY
   ```
5. Done — the plugin works out of the box using [ipquery.io](https://ipquery.io) even without a key

📖 Full setup guide → [Wiki: Getting Started](https://github.com/RageOfFire/ROF_AntiVPN/wiki/Getting-started)

---

## 🔧 Commands & Permissions

| Command | Description | Permission |
|---------|-------------|------------|
| `/rofvpn reload` | Reload config without restart | `rofvpn.admin` |
| `/rofvpn setapikey <key>` | Set proxycheck.io API key | `rofvpn.admin` |
| `/rofvpn check <player>` | Manually check an online player | `rofvpn.check` |
| `/rofvpn cache` | Clear the IP result cache | `rofvpn.admin` |

| Permission | Description | Default |
|------------|-------------|---------|
| `rofvpn.admin` | Access to admin commands | OP |
| `rofvpn.check` | Manually check a player | OP |
| `rofvpn.bypass` | Skip VPN check on login | `false` |
| `rofvpn.alert` | Receive in-game detection alerts | OP |

---

## ⚙️ Configuration

<details>
<summary>Click to expand default config.yml</summary>

```yaml
# Primary API key (proxycheck.io) — leave empty to use free tier
api-key: ''

# Fallback API — free, no key needed
fallback-api: 'ipquery'

# What to do when all APIs fail: allow or deny
api-fail-action: allow

cache:
  ttl: 1440  # minutes (24 hours)

detection:
  min-risk: 50       # 0-100 risk score threshold
  check-vpn: true
  check-proxy: true
  check-tor: true

whitelist:
  ips: ['127.0.0.1']
  countries: []      # e.g. ['US', 'GB']

punishment:
  action: kick       # kick or command
  command: 'ban %player% Using VPN/Proxy'

logs:
  console: true
  player: true       # in-game alerts to rofvpn.alert players
  file: true         # plugins/ROF_AntiVPN/logs/YYYY-MM-DD.log

discord-webhook:
  enable: false
  webhook-url: ''
```

</details>

📖 Full config reference → [Wiki: Default Configuration](https://github.com/RageOfFire/ROF_AntiVPN/wiki/Default-configuration)

---

## 📊 PlaceholderAPI

| Placeholder | Returns |
|-------------|---------|
| `%rofvpn_detected%` | `true` / `false` / `unknown` |
| `%rofvpn_risk%` | Risk score `0–100` or `N/A` |
| `%rofvpn_country%` | Country code e.g. `US` |
| `%rofvpn_type%` | `VPN` / `Proxy` / `Tor` / `Clean` |
| `%rofvpn_provider%` | Which API returned the result |
| `%rofvpn_cached%` | `true` if result is from cache |

---

## 🌐 Proxy Server Support

ROF_AntiVPN has a dedicated proxy jar for **BungeeCord** and **Velocity**.
Checks happen at the proxy level — before the player reaches any backend server.

📖 Setup guide → [Wiki: Proxy Server Setup](https://github.com/RageOfFire/ROF_AntiVPN/wiki/Proxy-Server-Setup)

---

## 📚 Wiki

| Page | Description |
|------|-------------|
| [Getting Started](https://github.com/RageOfFire/ROF_AntiVPN/wiki/Getting-started) | Installation & API key setup |
| [Commands & Permissions](https://github.com/RageOfFire/ROF_AntiVPN/wiki/Commands-&-Permissions) | All commands and nodes |
| [Default Configuration](https://github.com/RageOfFire/ROF_AntiVPN/wiki/Default-configuration) | Full annotated config.yml |
| [Proxy Server Setup](https://github.com/RageOfFire/ROF_AntiVPN/wiki/Proxy-Server-Setup) | BungeeCord & Velocity guide |
| [PlaceholderAPI](https://github.com/RageOfFire/ROF_AntiVPN/wiki/PlaceholderAPI) | Available placeholders |
| [Discord Webhook](https://github.com/RageOfFire/ROF_AntiVPN/wiki/Discord-Webhook) | Discord alert setup |
| [FAQ](https://github.com/RageOfFire/ROF_AntiVPN/wiki/Frequently-Asked-Questions-(FAQ)) | Common questions |

---

## 🐛 Issues & Contributing

Found a bug or have a feature request?
→ [Open an issue](https://github.com/RageOfFire/ROF_AntiVPN/issues)

Pull requests are welcome! Please test your changes before submitting.

---

## 💼 Work With Me

Enjoying this project or need something custom built?
I'm available for freelance Minecraft plugin development.

[![Hire Me on Fiverr](https://img.shields.io/badge/Hire_Me_on-Fiverr-1DBF73?style=for-the-badge&logo=fiverr&logoColor=white)](https://www.fiverr.com/s/d00632G)

---

## 👤 Author

**RageOfFire** · [GitHub](https://github.com/RageOfFire) · [Discord](https://discord.com/invite/FSUsV3jZ6G)

<div align="center">

If you find this plugin useful, please consider leaving a ⭐ on the repo!

</div>
