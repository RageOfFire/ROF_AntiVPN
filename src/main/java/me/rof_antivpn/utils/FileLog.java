package me.rof_antivpn.utils;

import me.rof_antivpn.ROF_AntiVPN;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileLog {
    private ROF_AntiVPN plugin;
    public FileLog(ROF_AntiVPN plugin) {
        this.plugin = plugin;
    }
    public void LogToFile(String message) {
        File logFile = new File(plugin.getDataFolder(), "log.txt");
        // Remove color string
        String stripedMessage = ChatColor.stripColor(message);
        // Format time
        SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try (FileWriter writer = new FileWriter(logFile, true)) {
            String timestamp = logDateFormat.format(new Date());
            String logMessage = "[" + timestamp + "] " + stripedMessage + "\n";
            writer.write(logMessage);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
