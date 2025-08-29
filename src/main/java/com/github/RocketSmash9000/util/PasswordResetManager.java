package com.github.RocketSmash9000.util;

import com.github.RocketSmash9000.StartupManager;
import com.github.RocketSmash9000.config.Config;

import java.io.File;
import java.sql.*;
import java.util.UUID;

/**
 * Manages password reset requests in the database.
 * Handles creating password reset requests for the admin app to process.
 */
public class PasswordResetManager {
    
    /**
     * Checks if the admin app is available by verifying the existence of the query app.
     * @return true if the admin app is available, false otherwise
     */
    private static boolean isAdminAppAvailable() {
        String queryAppPath = Config.getString("System.queryLast", null);
        if (queryAppPath == null) {
            Logger.warn("Admin app not configured");
            return false;
        }
        
        File appPath = new File(queryAppPath);
        boolean exists = appPath.exists() && appPath.isDirectory();
        if (!exists) {
            Logger.warn("Admin app not found");
        }
        return exists;
    }
    
    /**
     * Creates a new password reset request if the admin app is available.
     * @param dni The DNI of the user requesting the password reset
     * @param newPassword The new password to be set
     * @return The request ID if successful, null otherwise
     * @throws IllegalStateException if the admin app is not available
     */
    public static String createPasswordResetRequest(String dni, String newPassword) {
        // First, verify the admin app is available
        if (!isAdminAppAvailable()) {
            throw new IllegalStateException("Admin app is not available. Cannot process password reset requests.");
        }
        // First, verify the user exists
        if (!userExists(dni)) {
            Logger.warn("Attempted to create password reset for non-existent user: " + dni);
            return null;
        }
        
        // Store the encrypted password and get its path
        String requestId = UUID.randomUUID().toString();
        String encryptedPath = PasswordStorage.storeEncryptedPassword(requestId, newPassword);
        
        if (encryptedPath == null) {
            Logger.error("Failed to store encrypted password for request: " + requestId);
            return null;
        }
        
        // Insert the request into the database
        String sql = "INSERT INTO PasswordResetRequests (id, dni, request_time, status, encrypted_password_path) " +
                    "VALUES (?, ?, NOW(), 'PENDING', ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, requestId);
            pstmt.setString(2, dni);
            pstmt.setString(3, encryptedPath);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                Logger.info("Se creó la petición de reinicio de contraseña " + requestId + " para el usuario " + dni);
                return requestId;
            }
        } catch (SQLException e) {
            Logger.error("Error creating password reset request: " + e.getMessage());
            // Clean up the encrypted file if database insertion fails
            PasswordStorage.cleanupRequestFiles(requestId);
        }
        
        return null;
    }
    
    /**
     * Checks if a user with the given DNI exists.
     * @param dni The DNI to check
     * @return true if the user exists, false otherwise
     */
    private static boolean userExists(String dni) {
        String sql = "SELECT 1 FROM Empleados WHERE dni = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, dni);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Logger.error("Error checking if user exists: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets a database connection.
     * @return A connection to the database
     * @throws SQLException If a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost/LOGINS", 
            StartupManager.username, 
            StartupManager.pass
        );
    }
    
    /**
     * Cleans up old password reset requests.
     * This should be called periodically to remove old requests.
     * @param daysOld The minimum age in days for requests to be considered for cleanup
     */
    public static void cleanupOldRequests(int daysOld) {
        // Skip cleanup if admin app is not available to prevent data loss
        if (!isAdminAppAvailable()) {
            Logger.warn("Skipping cleanup of old password reset requests: Admin app is not available");
            return;
        }
        
        String sql = "DELETE FROM PasswordResetRequests WHERE request_time < DATE_SUB(NOW(), INTERVAL ? DAY)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, daysOld);
            int rowsDeleted = pstmt.executeUpdate();
            
            if (rowsDeleted > 0) {
                Logger.info("Cleaned up " + rowsDeleted + " old password reset requests");
                
                // Clean up any associated files
                // Note to any who reads this: In a production environment, you might want
                // to be more selective about which files to delete,
                // as there could be a race condition if QueryManager is still processing a request.
                // In that case, you'll have to refactor this code.
                // This is a simple implementation that assumes all old requests have been processed.
            }
        } catch (SQLException e) {
            Logger.error("Error cleaning up old password reset requests: " + e.getMessage());
        }
    }
}
