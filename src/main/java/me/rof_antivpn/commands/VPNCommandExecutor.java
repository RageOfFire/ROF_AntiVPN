package me.rof_antivpn.commands;

import me.rof_antivpn.ROF_AntiVPN;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VPNCommandExecutor implements CommandExecutor {
    private final ROF_AntiVPN plugin;

    public VPNCommandExecutor(ROF_AntiVPN plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String reloadmess = plugin.getConfig().getString("messages.command-reload");
        String permissionmess = plugin.getConfig().getString("messages.no-permission");
        String usagemess = plugin.getConfig().getString("messages.usage-main");
        String usagekeymess = plugin.getConfig().getString("messages.usage-key");
        String apikeymess = plugin.getConfig().getString("messages.apikey-success");
        if (args.length == 0) {
            // Display help or usage information
            sender.sendMessage(ChatColor.RED + usagemess);
            return true;
        }
        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("reload")) {
            if (sender.hasPermission("rofvpn.admin")) {
                plugin.reloadConfig();
                plugin.loadConfiguration();
                sender.sendMessage(ChatColor.GREEN + reloadmess);
            } else {
                sender.sendMessage(ChatColor.RED + permissionmess);
            }
        } else if (subCommand.equals("setapikey")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + usagekeymess);
                return true;
            }
            if (sender.hasPermission("rofvpn.admin")) {
                String newAPIKey = args[1];
                plugin.getConfig().set("APIKey", newAPIKey);
                plugin.saveConfig();
                plugin.loadConfiguration();
                sender.sendMessage(ChatColor.GREEN + apikeymess);
            } else {
                sender.sendMessage(ChatColor.RED + permissionmess);
            }
        } else {
            sender.sendMessage(ChatColor.RED + usagemess);
        }
        return true;
    }
}
