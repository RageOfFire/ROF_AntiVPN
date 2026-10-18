package me.rageoffire.rofantivpn.command;

import me.rageoffire.rofantivpn.ROFAntiVPN;
import me.rageoffire.rofantivpn.api.VpnCheckResult;
import me.rageoffire.rofantivpn.config.SpigotSpigotConfigManager;
import me.rageoffire.rofantivpn.manager.VpnCheckManager;
import me.rageoffire.rofantivpn.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Handles /rofvpn <reload|setapikey|check|cache>
 */
public class VpnCommand implements CommandExecutor {

    private final ROFAntiVPN plugin;
    private final SpigotConfigManager config;
    private final VpnCheckManager checkManager;

    public VpnCommand(ROFAntiVPN plugin, SpigotConfigManager config, VpnCheckManager checkManager) {
        this.plugin       = plugin;
        this.config       = config;
        this.checkManager = checkManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (args.length == 0) {
            sender.sendMessage(config.getMessage("usage-main"));
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "reload" -> {
                if (!sender.hasPermission("rofvpn.admin")) {
                    sender.sendMessage(config.getMessage("no-permission"));
                    return true;
                }
                config.reload();
                checkManager.buildProviders();
                checkManager.getCache().setTtlMinutes(config.getCacheTtlMinutes());
                sender.sendMessage(config.getMessage("command-reload"));
            }

            case "setapikey" -> {
                if (!sender.hasPermission("rofvpn.admin")) {
                    sender.sendMessage(config.getMessage("no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(config.getMessage("usage-key"));
                    return true;
                }
                config.saveApiKey(args[1]);
                checkManager.buildProviders();
                sender.sendMessage(config.getMessage("apikey-success"));
            }

            case "check" -> {
                if (!sender.hasPermission("rofvpn.check")) {
                    sender.sendMessage(config.getMessage("no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(config.getMessage("usage-check"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(MessageUtil.formatPlayer(
                            config.getMessage("check-player-offline"), args[1]));
                    return true;
                }
                if (target.getAddress() == null) return true;
                String ip = target.getAddress().getAddress().getHostAddress();

                // Run the manual check async and report back to sender
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    Optional<VpnCheckResult> result = checkManager.getCache().get(ip);
                    if (!result.isPresent()) {
                        // Not in cache — fire a live check via providers
                        // Temporarily force a fresh lookup by checking both providers manually
                        sender.sendMessage("\u00A7e[AntiVPN] Checking " + target.getName() + " (" + ip + ")...");
                        checkManager.checkAsync(target, ip);
                    } else {
                        VpnCheckResult r = result.get();
                        String msgKey = r.isVpnDetected() ? "check-result-vpn" : "check-result-clean";
                        String msg = MessageUtil.format(
                                config.getMessage(msgKey), target.getName(), ip, r);
                        Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(msg));
                    }
                });
            }

            case "cache" -> {
                if (!sender.hasPermission("rofvpn.admin")) {
                    sender.sendMessage(config.getMessage("no-permission"));
                    return true;
                }
                int removed = checkManager.getCache().clear();
                sender.sendMessage(config.getMessage("command-cache-cleared")
                        .replace("%count%", String.valueOf(removed)));
            }

            default -> sender.sendMessage(config.getMessage("usage-main"));
        }

        return true;
    }
}
