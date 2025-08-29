package com.github.RocketSmash9000;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import com.github.RocketSmash9000.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import static java.sql.DriverManager.getConnection;

public class Conexión {
	/**
	 * Will do one of these things in order:
	 * <ol>
	 *     <li> Connects to the database.
	 *     <li> Try to create tables if they don't exist already.
	 *     <li> If it can't connect to the database, it will try to connect to MySQL.
	 *     <li> If it connects, it will create the database.
	 * </ol>
	 * @return <ul>
	 *     <li> 0 if it connected to the database and the tables were created (or if they already existed)
	 *     <li> 1 if the database doesn't exist, but was created successfully.
	 *     <li> 2 if an error occurred.
	 * </ul>
	 */
	public static int creaDB() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection co= getConnection("jdbc:mysql://localhost/LOGINS", StartupManager.username, StartupManager.pass);
			Statement stm = co.createStatement();
			Logger.debug("Puede que nos hayamos conectado a la base de datos");
			String sql = "CREATE TABLE IF NOT EXISTS RegistroHorario (" + // Crea una tabla con lo siguiente...
					"id INT AUTO_INCREMENT PRIMARY KEY, " + // Una columna que va incrementando según se vayan insertando las cosas.
					"dni VARCHAR(20) NOT NULL, " + // Una columna con el DNI del trabajador
					"fecha DATE NOT NULL, " + // La fecha del día actual
					"hora_entrada TIME, " + //La hora a la que entró
					"hora_salida TIME, " + // La hora a la que salió
					"observaciones VARCHAR(255))"; // Notas del trabajador
			stm.executeUpdate(sql);

			stm = co.createStatement();
			sql = "CREATE TABLE IF NOT EXISTS Empleados (" +
					"dni VARCHAR(9) PRIMARY KEY, " +
					"nombre VARCHAR(100) NOT NULL, " +
					"activo BOOLEAN NOT NULL DEFAULT TRUE)";
			stm.executeUpdate(sql);

			// Create password reset requests table
			stm = co.createStatement();
			sql = "CREATE TABLE IF NOT EXISTS PasswordResetRequests (" +
					"id VARCHAR(36) PRIMARY KEY, " +
					"dni VARCHAR(9) NOT NULL, " +
					"request_time DATETIME NOT NULL, " +
					"status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING', " +
					"encrypted_password_path VARCHAR(255) NOT NULL, " +
					"processed_time DATETIME, " +
					"processed_by VARCHAR(100), " +
					"FOREIGN KEY (dni) REFERENCES Empleados(dni) ON DELETE CASCADE, " +
					"INDEX idx_dni (dni), " +
					"INDEX idx_status (status), " +
					"INDEX idx_request_time (request_time)" +
					")";
			stm.executeUpdate(sql);


			Logger.info("Nos hemos conectado a la base de datos.");
			return 0;

		} catch (ClassNotFoundException e) {
			Logger.error("Clase no encontrada: "+ e);
			Logger.debug(Arrays.toString(e.getStackTrace()));
			return 2;

		} catch (SQLException e) {
			Logger.error("Error ignorable: "+ e.getMessage());
			Logger.debug(Arrays.toString(e.getStackTrace()));
			Logger.info("Vamos a probar a conectar a la base de datos global.");
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection conn= getConnection("jdbc:mysql://localhost", StartupManager.username, StartupManager.pass);
				Statement stmt = conn.createStatement();
				String sql = "CREATE DATABASE LOGINS;";
				stmt.executeUpdate(sql);
				Logger.debug("Base de datos creada");
				return 1;

			} catch (ClassNotFoundException a) {
				Logger.error("La clase no se encontró");
				return 2;

			} catch (SQLException a) {
				Logger.error("Ha pasado algo con SQL pero vamos a hacer como que no ha habido ningún problema.");
				return 2;

			} catch (Exception a) {
				Logger.error("Pues nada. Un error raro chungo. " + a.getMessage());
				Logger.debug(Arrays.toString(a.getStackTrace()));
				return 2;
			}

		} catch (Exception e) {
			Logger.error("Error desconocido: "+ e.getMessage());
			Logger.debug(Arrays.toString(e.getStackTrace()));
			return 2;
		}
	}

	/**
	 * Adds a new entrance registry for the specified user.
	 * <pre></pre>
	 * Will check other registries for exists without entrance first, and update them if they exist.
	 * @param dni the DNI of the user.
	 * @param observaciones any observations/notes of the user if needed.
	 * @return <ul>
	 *     <li> 0 if no errors occurred.
	 *     <li> 1 if errors were encountered during the process.
	 * </ul>
	 */
	public static int insertarEntrada(String dni, String observaciones) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			try (Connection conn = getConnection("jdbc:mysql://localhost/LOGINS", StartupManager.username, StartupManager.pass)) {
				// Verify if there's a registry with an exit without an entrance
				boolean registroActualizado = false;
				try (ResultSet rs = obtenerUltimoRegistroHoy(conn, dni)) {
					if (rs.next()) {
						Time horaSalida = rs.getTime("hora_salida");
						Time horaEntrada = rs.getTime("hora_entrada");
						
						// If there is one, update the registry to add the entrance to that.
						if (horaSalida != null && horaEntrada == null) {
							String sql = "UPDATE RegistroHorario SET hora_entrada = ? WHERE id = ?";
							try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
								pstmt.setTime(1, Time.valueOf(LocalTime.now()));
								pstmt.setInt(2, rs.getInt("id"));
								pstmt.executeUpdate();
								registroActualizado = true;
							}
						}
					}
				}
				
				// If no registry was updated, create a new one.
				if (!registroActualizado) {
					String sql = "INSERT INTO RegistroHorario (dni, fecha, hora_entrada, observaciones) VALUES (?, CURDATE(), ?, ?)";
					try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
						pstmt.setString(1, dni);
						pstmt.setTime(2, Time.valueOf(LocalTime.now()));
						pstmt.setString(3, observaciones);
						pstmt.executeUpdate();
					}
				}

				// Update the employee to be active.
				String sql = "UPDATE Empleados SET activo = TRUE WHERE dni = ?";
				try (PreparedStatement stmt = conn.prepareStatement(sql)) {
					stmt.setString(1, dni);
					int rowsAffected = stmt.executeUpdate();
					Logger.debug("Se activaron " + rowsAffected + " trabajadores.");
				}

			} catch (SQLException e) {
				Logger.error("Algo falló: " + e.getMessage());
				Logger.debug(Arrays.toString(e.getStackTrace()));
			}

		} catch (ClassNotFoundException e) {
			Logger.error("Ha ocurrido un problema al insertar la entrada: " + e.getMessage());
			Logger.debug(Arrays.toString(e.getStackTrace()));
			return 1;
		}
		Logger.info("La entrada de la persona con DNI " + dni + " ha sido registrada.");
		return 0;
	}

	/**
	 * Adds a new exit registry for the specified user.
	 * <pre></pre>
	 * Will check other registries for exists without exit first, and update them if they exist.
	 * @param dni the DNI of the user.
	 * @param observaciones any observations/notes of the user if needed.
	 * @return <ul>
	 *     <li> 0 if no errors occurred.
	 *     <li> 1 if errors were encountered during the process.
	 * </ul>
	 */
	public static int insertarSalida(String dni, String observaciones) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			try (Connection conn = getConnection("jdbc:mysql://localhost/LOGINS", StartupManager.username, StartupManager.pass)) {
				// Verify if there's a registry with only an entrance
				boolean registroActualizado = false;
				try (ResultSet rs = obtenerUltimoRegistroHoy(conn, dni)) {
					if (rs.next()) {
						Time horaEntrada = rs.getTime("hora_entrada");
						Time horaSalida = rs.getTime("hora_salida");
						
						// If there's one, update it
						if (horaEntrada != null && horaSalida == null) {
							String sql = "UPDATE RegistroHorario SET hora_salida = ?, observaciones = ? WHERE id = ?";
							try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
								pstmt.setTime(1, Time.valueOf(LocalTime.now()));
								pstmt.setString(2, observaciones);
								pstmt.setInt(3, rs.getInt("id"));
								pstmt.executeUpdate();
								registroActualizado = true;
							}
						}
					}
				}
				
				// If no registry was updated, create a new one
				if (!registroActualizado) {
					String sql = "INSERT INTO RegistroHorario (dni, fecha, hora_salida, observaciones) VALUES (?, CURDATE(), ?, ?)";
					try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
						pstmt.setString(1, dni);
						pstmt.setTime(2, Time.valueOf(LocalTime.now()));
						pstmt.setString(3, observaciones);
						pstmt.executeUpdate();
					}
				}

				// Update employee's state to inactive
				String sql = "UPDATE Empleados SET activo = FALSE WHERE dni = ?";
				try (PreparedStatement stmt = conn.prepareStatement(sql)) {
					stmt.setString(1, dni);
					int rowsAffected = stmt.executeUpdate();
					Logger.debug("Se desactivaron " + rowsAffected + " trabajadores.");
				}

			} catch (SQLException e) {
				Logger.error("Algo falló: " + e.getMessage());
				Logger.debug(Arrays.toString(e.getStackTrace()));
			}

		} catch (ClassNotFoundException e) {
			Logger.error("Ha ocurrido un problema al insertar la salida: " + e.getMessage());
			Logger.debug(Arrays.toString(e.getStackTrace()));
			return 1;
		}
		Logger.info("La salida de la persona con DNI " + dni + " ha sido registrada.");
		return 0;
	}

	/**
	 * Obtains all the registries of a given user.
	 * @Warn Might lag if there are hundreds or thousands of registries.
	 * @param dni DNI of the user.
	 * @return all registries of the user with a given DNI.
	 */
	public static String obtenerRegistrosUsuario(String dni) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			String sql = "SELECT fecha, hora_entrada, hora_salida FROM RegistroHorario " +
					   "WHERE dni = ? ORDER BY id DESC";
			
			try (Connection conn = getConnection("jdbc:mysql://localhost/LOGINS", StartupManager.username, StartupManager.pass);
			     PreparedStatement pstmt = conn.prepareStatement(sql)) {
				
				pstmt.setString(1, dni);
				ResultSet rs = pstmt.executeQuery();
				
				// Create a JSON array to hold the records
				JSONArray records = new JSONArray();
				
				while (rs.next()) {
					JSONObject record = new JSONObject();
					record.put("fecha", rs.getDate("fecha").toString());
					
					Time horaEntrada = rs.getTime("hora_entrada");
					Time horaSalida = rs.getTime("hora_salida");
					
					if (horaEntrada != null) {
						record.put("hora_entrada", horaEntrada.toString());
					} else {
						record.put("hora_entrada", "");
					}
					
					if (horaSalida != null) {
						record.put("hora_salida", horaSalida.toString());
					} else {
						record.put("hora_salida", "");
					}
					
					records.add(record);
				}
				
				return records.toJSONString();
				
			} catch (SQLException e) {
				Logger.error("Error al obtener registros: " + e.getMessage());
				Logger.debug(Arrays.toString(e.getStackTrace()));
				return "[]";
			}
			
		} catch (ClassNotFoundException e) {
			Logger.error("Error: " + e.getMessage());
			Logger.debug(Arrays.toString(e.getStackTrace()));
			return "[]";
		}
	}
	
	/**
	 * Obtains the latest registry of the current day of a user.
	 * @param dni user's DNI.
	 * @return a ResultSet with the last register of the current day, or null if there's none.
	 * @throws SQLException if there's an error with the database.
	 */
	private static ResultSet obtenerUltimoRegistroHoy(Connection conn, String dni) throws SQLException {
		String sql = "SELECT * FROM RegistroHorario " +
				"WHERE dni = ? AND fecha = CURDATE() " +
				"ORDER BY id DESC LIMIT 1";
		
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, dni);
		return pstmt.executeQuery();
	}
}
