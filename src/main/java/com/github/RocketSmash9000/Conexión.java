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


			Logger.info("Nos hemos conectado a la base de datos.");
			return 0;

		} catch (ClassNotFoundException e) {
			Logger.error("Clase no encontrada: "+ e);
			Logger.debug(Arrays.toString(e.getStackTrace()));
			return 2;

		} catch (SQLSyntaxErrorException e) {
			Logger.debug("Es altamente probable que la tabla ya exista, así que vamos directos al final.");
			Logger.error("Error ignorable: " + e.getMessage());
			Logger.debug(Arrays.toString(e.getStackTrace()));
			return 0;

		} catch (SQLException e) {
			Logger.error("Error de conexion: "+ e.getMessage());
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

	public static int insertarEntrada(String dni, String observaciones) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection co= getConnection("jdbc:mysql://localhost/LOGINS", StartupManager.username, StartupManager.pass);
			String sql = "INSERT INTO RegistroHorario (dni, fecha, hora_entrada, observaciones) VALUES (?, ?, ?, ?)";
			try (PreparedStatement pstmt = co.prepareStatement(sql)) {
				LocalDate fecha = LocalDate.now();       // Fecha actual
				LocalTime horaEntrada = LocalTime.now(); // Hora actual

				pstmt.setString(1, dni);
				pstmt.setDate(2, Date.valueOf(fecha));
				pstmt.setTime(3, Time.valueOf(horaEntrada));
				pstmt.setString(4, observaciones);

				pstmt.executeUpdate();
			}

			sql = "UPDATE Empleados SET activo = TRUE WHERE dni = ?";

			try (Connection conn = getConnection("jdbc:mysql://localhost/LOGINS", StartupManager.username, StartupManager.pass);
			     PreparedStatement stmt = conn.prepareStatement(sql)) {

				stmt.setString(1, dni);
				int rowsAffected = stmt.executeUpdate();
				Logger.debug("Se activaron " + rowsAffected + " trabajadores.");

			} catch (SQLException e) {
				Logger.error("Algo falló: " + e.getMessage());
				Logger.debug(Arrays.toString(e.getStackTrace()));
			}

		} catch (ClassNotFoundException | SQLException e) {
			Logger.error("Ha ocurrido un problema al insertar la entrada: " + e.getMessage());
			Logger.debug(Arrays.toString(e.getStackTrace()));
			return 1;
		}
		Logger.info("La entrada de la persona con DNI " + dni + " ha sido registrada.");
		return 0; // Devuelve 0 si no ha habido ningún error
	}

	public static int insertarSalida(String dni, String observaciones) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection co= getConnection("jdbc:mysql://localhost/LOGINS", StartupManager.username, StartupManager.pass);
			String sql = "INSERT INTO RegistroHorario (dni, fecha, hora_salida, observaciones) VALUES (?, ?, ?, ?)";
			try (PreparedStatement pstmt = co.prepareStatement(sql)) {
				LocalDate fecha = LocalDate.now();       // Fecha actual
				LocalTime horaSalida = LocalTime.now(); // Hora actual

				pstmt.setString(1, dni);
				pstmt.setDate(2, Date.valueOf(fecha));
				pstmt.setTime(3, Time.valueOf(horaSalida));
				pstmt.setString(4, observaciones);

				pstmt.executeUpdate();
			}

			sql = "UPDATE Empleados SET activo = FALSE WHERE dni = ?";

			try (Connection conn = getConnection("jdbc:mysql://localhost/LOGINS", StartupManager.username, StartupManager.pass);
			     PreparedStatement stmt = conn.prepareStatement(sql)) {

				stmt.setString(1, dni);
				int rowsAffected = stmt.executeUpdate();
				Logger.debug("Se desactivaron " + rowsAffected + " trabajadores.");

			} catch (SQLException e) {
				Logger.error("Algo falló: " + e.getMessage());
				Logger.debug(Arrays.toString(e.getStackTrace()));
			}

		} catch (ClassNotFoundException | SQLException e) {
			Logger.error("Ha ocurrido un problema al insertar la salida: " + e.getMessage());
			Logger.debug(Arrays.toString(e.getStackTrace()));
			return 1;
		}
		Logger.info("La salida de la persona con DNI " + dni + " ha sido registrada.");
		return 0; // Devuelve 0 si no ha habido ningún error
	}

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
}
