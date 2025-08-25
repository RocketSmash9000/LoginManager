package com.github.RocketSmash9000;

import com.github.RocketSmash9000.util.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.github.RocketSmash9000.StartupManager.FILE_NAME;
import static com.github.RocketSmash9000.StartupManager.folderName;

public class Login {

	/**
	 *
	 * @param dni el DNI de la persona.
	 * @param pass la contraseña de la persona.
	 * @return 0 si existe el usuario y la contraseña es válida, 1 si el usuario existe pero la contraseña es inválida, 2 si no existe el ususario y 3 si algo salió mal.
	 */
	@SuppressWarnings("D")
	public static int validarUsuario(String dni, String pass) {
		String appDataPath = System.getenv("APPDATA");

		// Check if the APPDATA environment variable exists.
		if (appDataPath == null || appDataPath.isEmpty()) {
			Logger.warn("La variable de entorno APPDATA no fue encontrada. ¿Esto es Windows?");
			return 3;
		}

		File folder = new File(folderName);
		File file = new File(folder, FILE_NAME);

		if (!file.exists())
			return 3;

		Path path = Paths.get(Cryptography.decrypt(file).toString());
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
					if (!Files.deleteIfExists(path)) {
						Logger.error("No se pudo eliminar el archivo desencriptado: " + path);
					}
					return 0;
				}
			}

			if (encontrado) {
				if (!Files.deleteIfExists(path)) {
					Logger.error("No se pudo eliminar el archivo desencriptado: " + path);
				}
				return 1;
			}

		} catch (IOException e) {
			Logger.error("Error al leer el archivo: " + e.getMessage());
			Logger.error(Arrays.toString(e.getStackTrace()));
			return 3;
		}

		try {
			if (!Files.deleteIfExists(path)) {
				Logger.error("No se pudo eliminar el archivo desencriptado: " + path);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return 2;
	}

	public static boolean añadirUsuario(String dni, String contraseña) {
		// 1. Get the AppData\Roaming folder path from the environment variable.
		// The "APPDATA" environment variable typically points to C:\Users\{username}\AppData\Roaming
		String appDataPath = System.getenv("APPDATA");

		// Check if the APPDATA environment variable exists.
		if (appDataPath == null || appDataPath.isEmpty()) {
			Logger.warn("La variable de entorno APPDATA no fue encontrada. ¿Esto es Windows?");
			return false;
		}

		File folder = new File(folderName);
		File file = new File(folder, FILE_NAME);

		if (!file.exists()) {
			Logger.warn("El archivo no existe. Raro, no?");
			return false;
		}

		// Check if user already exists
		int userStatus = validarUsuario(dni, "");
		if (userStatus != 2) { // 2 means user doesn't exist
			Logger.debug("El usuario con DNI " + dni + " ya existe.");
			return false;
		}

		File archivo = Cryptography.decrypt(file);

		Path path = Paths.get(archivo.toString());
		try {
			// Append the new user credentials
			String newLine = System.lineSeparator();
			String credentials = (archivo.length() > 0 ? newLine : "") + dni + newLine + contraseña;

			Files.write(path, credentials.getBytes(), java.nio.file.StandardOpenOption.APPEND);
			Logger.debug("Usuario " + dni + " añadido correctamente.");
			return true;
		} catch (IOException e) {
			Logger.error("Error al añadir al usuario: " + e.getMessage());
			Logger.error(Arrays.toString(e.getStackTrace()));
			return false;
		} finally {
			try {
				Cryptography.encrypt(archivo);
				if (!Files.deleteIfExists(path)) {
					Logger.error("No se pudo eliminar el archivo desencriptado: " + archivo);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
