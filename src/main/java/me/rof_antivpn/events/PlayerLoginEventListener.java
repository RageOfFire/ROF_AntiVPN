package me.rof_antivpn.events;

import me.rof_antivpn.ROF_AntiVPN;
import me.rof_antivpn.utils.AntiVPN;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Objects;

public class PlayerLoginEventListener implements Listener {
    private final ROF_AntiVPN plugin;

    public PlayerLoginEventListener(ROF_AntiVPN plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onPlayerLogin (PlayerLoginEvent e) {
        String kickmess = plugin.getConfig().getString("messages.kick-message");
        // Do nothing if api key no being set
        if (Objects.equals(plugin.getConfig().getString("APIKey"), "your-api-key") || plugin.getConfig().getString("APIKey") == null || Objects.equals(plugin.getConfig().getString("APIKey"), "")) {
            return;
        }
        // Bypass the VPN check
        if (e.getPlayer().hasPermission("rofvpn.bypass")) {
            return;
        }
        String ipAddress = e.getAddress().getHostAddress();

        String isVPN = AntiVPN.isVPN(ipAddress);
        if (Objects.equals(isVPN, "yes")) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + kickmess);
        }
    }
}
