package me.rageoffire.rofantivpn.listener;

import me.rageoffire.rofantivpn.manager.VpnCheckManager;
import me.rageoffire.rofantivpn.util.LocalAddressUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Triggers an async VPN check as soon as a player joins.
 *
 * Note: We use PlayerJoinEvent (not AsyncPlayerPreLoginEvent) because
 * the Player object is required for permission checks and kicking.
 * The kick will still fire before the player can interact.
 */
public class PlayerJoinListener implements Listener {

    private final VpnCheckManager checkManager;

    public PlayerJoinListener(VpnCheckManager checkManager) {
        this.checkManager = checkManager;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getAddress() == null) return;

        String ip = player.getAddress().getAddress().getHostAddress();

        // Skip loopback, private, link-local, and all other reserved ranges
        if (LocalAddressUtil.isLocal(ip)) return;

        checkManager.checkAsync(player, ip);
    }
}
