package me.rageoffire.rofantivpn.command;

import me.rageoffire.rofantivpn.ROFAntiVPN;
import me.rageoffire.rofantivpn.api.VpnCheckResult;
import me.rageoffire.rofantivpn.config.SpigotConfigManager;
import me.rageoffire.rofantivpn.manager.VpnCheckManager;
import me.rageoffire.rofantivpn.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Handles /rofvpn <reload|setapikey|check|cache>
 * Java 8 compatible — no switch expressions, no lambdas in schedulers.
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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(config.getMessage("usage-main"));
            return true;
        }

        String sub = args[0].toLowerCase();

        if ("reload".equals(sub)) {
            if (!sender.hasPermission("rofvpn.admin")) {
                sender.sendMessage(config.getMessage("no-permission"));
                return true;
            }
            config.reload();
            checkManager.buildProviders();
            checkManager.getCache().setTtlMinutes(config.getCacheTtlMinutes());
            sender.sendMessage(config.getMessage("command-reload"));

        } else if ("setapikey".equals(sub)) {
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

        } else if ("check".equals(sub)) {
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

            final Player finalTarget  = target;
            final String ip           = target.getAddress().getAddress().getHostAddress();
            final CommandSender finalSender = sender;

            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    Optional<VpnCheckResult> result = checkManager.getCache().get(ip);
                    if (!result.isPresent()) {
                        finalSender.sendMessage("\u00A7e[AntiVPN] Checking "
                                + finalTarget.getName() + " (" + ip + ")...");
                        checkManager.checkAsync(finalTarget, ip);
                    } else {
                        final VpnCheckResult r = result.get();
                        final String msgKey = r.isVpnDetected()
                                ? "check-result-vpn" : "check-result-clean";
                        final String resultMsg = MessageUtil.format(
                                config.getMessage(msgKey), finalTarget.getName(), ip, r);
                        Bukkit.getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                finalSender.sendMessage(resultMsg);
                            }
                        });
                    }
                }
            });

        } else if ("cache".equals(sub)) {
            if (!sender.hasPermission("rofvpn.admin")) {
                sender.sendMessage(config.getMessage("no-permission"));
                return true;
            }
            int removed = checkManager.getCache().clear();
            sender.sendMessage(config.getMessage("command-cache-cleared")
                    .replace("%count%", String.valueOf(removed)));

        } else {
            sender.sendMessage(config.getMessage("usage-main"));
        }

        return true;
    }
}
