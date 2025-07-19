package com.github.RocketSmash9000;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.RocketSmash9000.StartupManager.FILE_NAME;
import static com.github.RocketSmash9000.StartupManager.FOLDER_NAME;

public class Login {

	/**
	 *
	 * @param dni el DNI de la persona.
	 * @param pass la contraseña de la persona.
	 * @return 0 si existe el usuario y la contraseña es válida, 1 si el usuario existe pero la contraseña es inválida, 2 si no existe el ususario y 3 si algo salió mal.
	 */
	public static int validarUsuario(String dni, String pass) {
		String appDataPath = System.getenv("APPDATA");

		// Check if the APPDATA environment variable exists.
		if (appDataPath == null || appDataPath.isEmpty()) {
			Logger.log("La variable de entorno APPDATA no fue encontrada. ¿Esto es Windows?");
			return 3;
		}

		File folder = new File(appDataPath, FOLDER_NAME);
		File file = new File(folder, FILE_NAME);

		if (!file.exists())
			return 3;

		Cryptography.decrypt(file);

		Path path = Paths.get(file.toString());
		try (var reader = Files.newBufferedReader(path)) {
			boolean encontrado = false;
			boolean contraseña = false;
			for (String line = ""; line != null; line = reader.readLine()) {
				if (line.equals(dni)) {
					encontrado = true;
				} else if (line.equals(pass) && encontrado) {
					contraseña = true;
				}
				if (encontrado && contraseña) {
					Cryptography.encrypt(file);
					return 0;
				}
			}

			if (encontrado) {
				Cryptography.encrypt(file);
				return 1;
			}

		} catch (IOException e) {
			Logger.log("Error al leer el archivo: " + e.getMessage());
			return 3;
		}

		Cryptography.encrypt(file);
		return 2;
	}

	public static boolean añadirUsuario(String dni, String contraseña) {
		// 1. Get the AppData\Roaming folder path from the environment variable.
		// The "APPDATA" environment variable typically points to C:\Users\{username}\AppData\Roaming
		String appDataPath = System.getenv("APPDATA");

		// Check if the APPDATA environment variable exists.
		if (appDataPath == null || appDataPath.isEmpty()) {
			Logger.log("La variable de entorno APPDATA no fue encontrada. ¿Esto es Windows?");
			return false;
		}

		File folder = new File(appDataPath, FOLDER_NAME);
		File file = new File(folder, FILE_NAME);

		if (!file.exists()) {
			Logger.log("El archivo no existe. Raro, no?");
			return false;
		}

		// Check if user already exists
		int userStatus = validarUsuario(dni, "");
		if (userStatus != 2) { // 2 means user doesn't exist
			Logger.log("El usuario con DNI " + dni + " ya existe.");
			return false;
		}

		Cryptography.decrypt(file);

		try {
			// Append the new user credentials
			Path path = Paths.get(file.toString());
			String newLine = System.lineSeparator();
			String credentials = (file.length() > 0 ? newLine : "") + dni + newLine + contraseña;

			Files.write(path, credentials.getBytes(), java.nio.file.StandardOpenOption.APPEND);
			Logger.log("Usuario " + dni + " añadido correctamente.");
			return true;
		} catch (IOException e) {
			Logger.log("Error al añadir al usuario: " + e.getMessage());
			return false;
		} finally {
			// Always encrypt the file, even if there was an error
			Cryptography.encrypt(file);
		}
	}
}
