package com.github.RocketSmash9000;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class Conexión {
	public static int creaDB() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection co= DriverManager.getConnection("jdbc:mysql://localhost/LOGINS", StartupManager.username, StartupManager.pass);
			Statement stm = co.createStatement();
			Logger.log("Puede que nos hayamos conectado a la base de datos");
			String sql = "CREATE TABLE RegistroHorario (" + // Crea una tabla con lo siguiente...
					"id INT AUTO_INCREMENT PRIMARY KEY, " + // Una columna que va incrementando según se vayan insertando las cosas.
					"dni VARCHAR(20) NOT NULL, " + // Una columna con el DNI del trabajador
					"fecha DATE NOT NULL, " + // La fecha del día actual
					"hora_entrada TIME, " + //La hora a la que entró
					"hora_salida TIME)"; // La hora a la que salió
			stm.executeUpdate(sql);
			Logger.log("Pues nos hemos conectado.");
			return 0;

		} catch (ClassNotFoundException e) {
			Logger.log("Clase no encontrada: "+e);
			return 2;

		} catch (SQLSyntaxErrorException e) {
			Logger.log("Es altamente probable que la tabla ya exista, así que vamos directos al final.");
			return 0;

		} catch (SQLException e) {
			Logger.log("Error de conexion: "+e);
			Logger.log("Vamos a probar a conectar a la base de datos global.");
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection conn= DriverManager.getConnection("jdbc:mysql://localhost", StartupManager.username, StartupManager.pass);
				Statement stmt = conn.createStatement();
				String sql = "CREATE DATABASE LOGINS;";
				stmt.executeUpdate(sql);
				Logger.log("Base de datos creada");
				return 1;

			} catch (ClassNotFoundException a) {
				Logger.log("La clase no se encontró");
				return 2;

			} catch (SQLException a) {
				Logger.log("Ha pasado algo con SQL pero vamos a hacer como que no ha habido ningún problema.");
				return 2;

			} catch (Exception a) {
				Logger.log("Pues nada. Un error raro chungo.");
				return 2;
			}

		} catch (Exception e) {
			Logger.log("Error desconocido: "+e);
			return 2;
		}
	}

	public static int insertarEntrada(String dni) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection co= DriverManager.getConnection("jdbc:mysql://localhost/LOGINS", StartupManager.username, StartupManager.pass);
			String sql = "INSERT INTO RegistroHorario (dni, fecha, hora_entrada) VALUES (?, ?, ?)";
			try (PreparedStatement pstmt = co.prepareStatement(sql)) {
				LocalDate fecha = LocalDate.now();       // Fecha actual
				LocalTime horaEntrada = LocalTime.now(); // Hora actual

				pstmt.setString(1, dni);
				pstmt.setDate(2, Date.valueOf(fecha));             // java.sql.Date
				pstmt.setTime(3, Time.valueOf(horaEntrada));       // java.sql.Time

				pstmt.executeUpdate();
			}

		} catch (ClassNotFoundException | SQLException e) {
			Logger.log("Ha ocurrido un problema al insertar la entrada.");
			return 1;
		}
		Logger.log("La entrada de la persona con DNI " + dni + " ha sido registrada.");
		return 0; // Devuelve 0 si no ha habido ningún error
	}

	public static int insertarSalida(String dni) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection co= DriverManager.getConnection("jdbc:mysql://localhost/LOGINS", StartupManager.username, StartupManager.pass);
			String sql = "INSERT INTO RegistroHorario (dni, fecha, hora_salida) VALUES (?, ?, ?)";
			try (PreparedStatement pstmt = co.prepareStatement(sql)) {
				LocalDate fecha = LocalDate.now();       // Fecha actual
				LocalTime horaSalida = LocalTime.now(); // Hora actual

				pstmt.setString(1, dni);
				pstmt.setDate(2, Date.valueOf(fecha));             // java.sql.Date
				pstmt.setTime(3, Time.valueOf(horaSalida));       // java.sql.Time

				pstmt.executeUpdate();
			}

		} catch (ClassNotFoundException | SQLException e) {
			Logger.log("Ha ocurrido un problema al insertar la salida.");
			return 1;
		}
		Logger.log("La salida de la persona con DNI " + dni + " ha sido registrada.");
		return 0; // Devuelve 0 si no ha habido ningún error
	}
}
