package com.github.RocketSmash9000.util;

import com.github.RocketSmash9000.config.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class LogHelper {
    private static final String CARPETA_LOGS = Config.getString("LoginLogHelper.directory", "\\LoginManager_logs");
    private static final int maxLogs = Config.getInt("LoginLogHelper.maxLogs", 5);
    private static String logFile;
    
    /**
     * Creates a new log file with a timestamp in the logs directory.
     * The file will be named in the format: log_DD-MM-YY - HH.mm.log
     */
    public static void createLog() {
        try {
            // Create logs directory if it doesn't exist
            String logDir = Config.CONFIG_DIR + CARPETA_LOGS;
            Files.createDirectories(Paths.get(logDir));
            
            // Generate timestamp for the filename
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy - HH-mm");
            String timestamp = LocalDateTime.now().format(formatter);
            
            // Set the log file path
            logFile = logDir + File.separator + timestamp + ".log";
            
            // Create the log file if it doesn't exist
            File file = new File(logFile);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            Logger.error("Algo salió mal al crear el achivo de logs " + e);
	        Logger.error(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Adds content to the current log file
     * @param contentToAdd The content to add to the log file
     */
    public static void addToLog(String contentToAdd) {
        if (logFile == null) {
            createLog();
        }

		String content = contentToAdd + "\n";
        
        try {
            Files.writeString(
                Paths.get(logFile),
                content,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al escribir en el archivo de log", e);
        }
    }
}
