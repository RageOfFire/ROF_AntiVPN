package me.rageoffire.rofantivpn.log;

import me.rageoffire.rofantivpn.api.VpnCheckResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Handles all file and console logging for VPN detections.
 * Each day gets its own log file: logs/YYYY-MM-DD.log
 * Log line format: [YYYY-MM-DD HH:mm:ss] [DETECTION] player (ip) Risk:75 Country:US Type:VPN Provider:proxycheck.io
 */
public class VpnLogger {

    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter FILE_DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final File logsDir;
    private final Logger consoleLogger;
    private final boolean fileEnabled;
    private final boolean consoleEnabled;

    public VpnLogger(File pluginDataFolder, Logger consoleLogger,
                     boolean fileEnabled, boolean consoleEnabled) {
        this.logsDir        = new File(pluginDataFolder, "logs");
        this.consoleLogger  = consoleLogger;
        this.fileEnabled    = fileEnabled;
        this.consoleEnabled = consoleEnabled;

        if (fileEnabled) {
            logsDir.mkdirs();
        }
    }

    /**
     * Log a detection event for the given player.
     */
    public void logDetection(String playerName, String ip, VpnCheckResult result) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
        String line = String.format("[%s] [DETECTION] %s (%s) Risk:%s Country:%s Type:%s Provider:%s",
                timestamp, playerName, ip,
                result.getRiskDisplay(), result.getCountry(),
                result.getType(), result.getProvider());

        if (consoleEnabled) {
            consoleLogger.warning(line);
        }

        if (fileEnabled) {
            writeToFile(line);
        }
    }

    /**
     * Log a general info message (e.g. cache hit, API fallback).
     */
    public void logInfo(String message) {
        consoleLogger.info("[AntiVPN] " + message);
    }

    /**
     * Log a warning (e.g. API failure).
     */
    public void logWarning(String message) {
        consoleLogger.warning("[AntiVPN] " + message);
    }

    private void writeToFile(String line) {
        String fileName = LocalDate.now().format(FILE_DATE_FMT) + ".log";
        File logFile = new File(logsDir, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            consoleLogger.warning("[AntiVPN] Failed to write to log file: " + e.getMessage());
        }
    }
}
