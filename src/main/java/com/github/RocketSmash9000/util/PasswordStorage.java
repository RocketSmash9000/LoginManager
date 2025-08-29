package com.github.RocketSmash9000.util;

import com.github.RocketSmash9000.Cryptography;
import com.github.RocketSmash9000.config.Config;
import com.github.RocketSmash9000.util.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Handles secure storage and management of password reset requests.
 * This class is responsible for creating, storing, and managing password reset requests,
 * including their associated encrypted password files.
 */
public class PasswordStorage {
    private static final String ENCRYPTED_PASSWORDS_DIR = "requests";
    
    static {
        // Create necessary directories if they don't exist
        try {
            Files.createDirectories(Paths.get(Config.configDir, ENCRYPTED_PASSWORDS_DIR));
        } catch (IOException e) {
            Logger.error("Error creando los directorios de reinicio de contraseña: " + e.getMessage());
            throw new RuntimeException("Failed to initialize password storage", e);
        }
    }
    
    /**
     * Stores an encrypted password in a secure file.
     * @param requestId The ID of the request this password belongs to
     * @param password The password to encrypt and store
     * @return The path to the encrypted password file, or null if storage failed
     */
    public static String storeEncryptedPassword(String requestId, String password) {
        try {
            // Create a temporary file with the password
            Path tempFile = Files.createFile(Paths.get(Config.configDir, ENCRYPTED_PASSWORDS_DIR, "pwd_" + requestId + ".pwd"));
            Files.writeString(tempFile, password, StandardOpenOption.WRITE);
            
            Cryptography.firstEncrypt(tempFile.toFile());

            return String.valueOf(tempFile.toAbsolutePath());
        } catch (IOException e) {
            Logger.error("Error guardando la contraseña encriptada: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Cleans up files related to a specific request.
     * @param requestId The ID of the request to clean up
     */
    public static void cleanupRequestFiles(String requestId) {
        try {
            // Delete the encrypted password file if it exists
            Path encryptedFile = Paths.get(Config.configDir, ENCRYPTED_PASSWORDS_DIR, requestId + ".pwd");
            Files.deleteIfExists(encryptedFile);
            
            Logger.debug("Cleaned up files for request: " + requestId);
        } catch (IOException e) {
            Logger.error("Error cleaning up request files for " + requestId + ": " + e.getMessage());
        }
    }
    
    /**
     * Cleans up a single password reset request and its associated files.
     * @param requestFile The request file to clean up
     */
    private static void cleanupRequest(File requestFile) {
        try {
            // First, find and delete the associated encrypted password file
            String content = Files.readString(requestFile.toPath());
            for (String line : content.split("\n")) {
                if (line.startsWith("encryptedPasswordPath=")) {
                    String encryptedPath = line.substring(21);
                    try {
                        Files.deleteIfExists(Paths.get(encryptedPath));
                    } catch (IOException e) {
                        Logger.error("Error deleting encrypted password file " + encryptedPath + ": " + e.getMessage());
                    }
                    break;
                }
            }
            
            // Then delete the request file itself
            Files.deleteIfExists(requestFile.toPath());
            
        } catch (IOException e) {
            Logger.error("Error cleaning up request file " + requestFile.getName() + ": " + e.getMessage());
        }
    }
}
