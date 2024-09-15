package me.rof_antivpn.events;

import com.google.gson.JsonObject;
import me.rof_antivpn.ROF_AntiVPN;
import me.rof_antivpn.utils.AntiVPN;
import me.rof_antivpn.utils.DiscordWebhook;
import me.rof_antivpn.utils.FileLog;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getLogger;

public class PlayerLoginEventListener implements Listener {
    private final ROF_AntiVPN plugin;
    private FileLog fileLog;

    public PlayerLoginEventListener(ROF_AntiVPN plugin) {
        this.plugin = plugin;
        this.fileLog = new FileLog(plugin);
    }


    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        Player player = e.getPlayer();
        String kickmess = plugin.getConfig().getString("messages.kick-message");
        // Do nothing if api key no being set
        if (Objects.equals(plugin.getConfig().getString("APIKey"), "your-api-key") || plugin.getConfig().getString("APIKey") == null) {
            return;
        }
        // Bypass the VPN check
        if (player.hasPermission("rofvpn.bypass")) {
            return;
        }
        String ipAddress = e.getAddress().getHostAddress();
        if(ipAddress.equalsIgnoreCase("localhost")) {
            return;
        }
        JsonObject isVPN = AntiVPN.isVPN(ipAddress);
        if(isVPN == null) {
            return;
        }
        if (Objects.equals(isVPN.get("proxy").getAsString(), "yes")) {
            String alertmess = plugin.getConfig().getString("messages.alert-message")
                    .replace("%player%", player.getName())
                    .replace("%ip%", ipAddress)
                    .replace("%risk%", isVPN.get("risk").getAsString())
                    .replace("%country%", isVPN.get("country").getAsString())
                    .replace("%type%", isVPN.get("type").getAsString());
            // Kick player
            if(plugin.getConfig().getBoolean("kick-player")) {
                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + kickmess);
            }
            // Alert to console
            if (plugin.getConfig().getBoolean("logs.console")) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', alertmess));
            }
            // Alert to everyone have permission
            if (plugin.getConfig().getBoolean("logs.player")) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (online.hasPermission("rofvpn.alert")) {
                        online.sendMessage(ChatColor.translateAlternateColorCodes('&', alertmess));
                    }
                }
            }
            // Alert to file
            if (plugin.getConfig().getBoolean("logs.file")) {
                fileLog.LogToFile(alertmess);
            }
            // Discord webhook alert
            if (plugin.getConfig().getBoolean("discord-webhook.enable")) {
                // Handle if webhook not being set
                if (Objects.equals(plugin.getConfig().getString("discord-webhook.webhook-url"), "your-webhook-url") || plugin.getConfig().getString("discord-webhook.webhook-url") == null) {
                    return;
                }
                DiscordWebhook webhook = new DiscordWebhook(plugin.getConfig().getString("discord-webhook.webhook-url"));
                webhook.setUsername(plugin.getConfig().getString("discord-webhook.name"));
                webhook.setAvatarUrl(plugin.getConfig().getString("discord-webhook.avatar"));
                DiscordWebhook.EmbedObject discordAlert = new DiscordWebhook.EmbedObject();
                // Embed created
                discordAlert
                        .setTitle(plugin.getConfig().getString("discord-webhook.title")
                                .replace("%player%", player.getName())
                                .replace("%ip%", ipAddress)
                                .replace("%risk%", isVPN.get("risk").getAsString())
                                .replace("%country%", isVPN.get("country").getAsString())
                                .replace("%type%", isVPN.get("type").getAsString()))
                        .setDescription(plugin.getConfig().getString("discord-webhook.description")
                                .replace("%player%", player.getName())
                                .replace("%ip%", ipAddress)
                                .replace("%risk%", isVPN.get("risk").getAsString())
                                .replace("%country%", isVPN.get("country").getAsString())
                                .replace("%type%", isVPN.get("type").getAsString()));
                // Field generate
                for (String fieldId : plugin.getConfig().getConfigurationSection("discord-webhook.fields").getKeys(false)) {
                    String fieldname = plugin.getConfig().getString("discord-webhook.fields." + fieldId + ".name")
                            .replace("%player%", player.getName())
                            .replace("%ip%", ipAddress)
                            .replace("%risk%", isVPN.get("risk").getAsString())
                            .replace("%country%", isVPN.get("country").getAsString())
                            .replace("%type%", isVPN.get("type").getAsString());
                    String fieldvalue = plugin.getConfig().getString("discord-webhook.fields." + fieldId + ".value")
                            .replace("%player%", player.getName())
                            .replace("%ip%", ipAddress)
                            .replace("%risk%", isVPN.get("risk").getAsString())
                            .replace("%country%", isVPN.get("country").getAsString())
                            .replace("%type%", isVPN.get("type").getAsString());
                    Boolean fieldinline = plugin.getConfig().getBoolean("discord-webhook.fields." + fieldId + ".inline");
                    discordAlert.addField(fieldname, fieldvalue, fieldinline);
                }
                discordAlert.setThumbnail(plugin.getConfig().getString("discord-webhook.thumbnail")
                        .replace("%player%", player.getName())
                        .replace("%ip%", ipAddress)
                        .replace("%risk%", isVPN.get("risk").getAsString())
                        .replace("%country%", isVPN.get("country").getAsString())
                        .replace("%type%", isVPN.get("type").getAsString()));
                // add Embed to webhook
                webhook.addEmbed(discordAlert);
                // execute
                try {
                    webhook.execute();
                } catch (IOException ex) {
                    getLogger().log(Level.WARNING, "Failed to send webhook !", ex);
                }
            }
        }

    }
}
