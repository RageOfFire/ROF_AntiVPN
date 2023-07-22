package me.rof_antivpn.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class VPNTabCompletion implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(args.length == 1) {
            List<String> commandtab = new ArrayList<>();
            commandtab.add("reload");
            commandtab.add("setapikey");

            return commandtab;
        }
        return null;
    }
}
