package com.github.RocketSmash9000;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class StartupManager {
	// The name of your application's folder.
	private static final String folderName = "LoginManager";

	// The name of the file you want to create.
	private static final String fileName = "Credentials.txt";

	public static String username;
	public static String pass;

	public static boolean credFileExists(){
		// 1. Get the AppData\Roaming folder path from the environment variable.
		// The "APPDATA" environment variable typically points to C:\Users\{username}\AppData\Roaming
		String appDataPath = System.getenv("APPDATA");

		// Check if the APPDATA environment variable exists.
		if (appDataPath == null || appDataPath.isEmpty()) {
			Logger.log("La variable de entorno APPDATA no fue encontrada. ¿Esto es Windows?");
			return false;
		}

		File folder = new File(appDataPath, folderName);
		File file = new File(folder, fileName);

		if (file.exists())
			return true;
		else return false;
	}
	public static void firstStartup() {
		System.out.println("Hola! Si estás viendo esto es porque esta es la primera vez que inicias el programa.");
		System.out.println("De ser así, vamos a tener que pedirte las crecenciales para poder acceder a mySQL.");
		System.out.println("De esta forma, podremos crear y administrar la base de datos de LoginManager.");
		System.out.println("Si mySQL no está instalado, instálalo antes de introducir las credenciales de acceso.");

		Scanner scanner = new Scanner(System.in);  // Create a Scanner object

		System.out.print("Introduce el usuario de mySQL: ");  // Prompt the user
		username = scanner.nextLine();  // Read user input as String
		System.out.print("Introduce ahora la contraseña del usuario: ");
		pass = scanner.nextLine();

		String fileContent = username + "\n" + pass;

		try {
			// 1. Get the AppData\Roaming folder path from the environment variable.
			// The "APPDATA" environment variable typically points to C:\Users\{username}\AppData\Roaming
			String appDataPath = System.getenv("APPDATA");

			// Check if the APPDATA environment variable exists.
			if (appDataPath == null || appDataPath.isEmpty()) {
				Logger.log("La variable de entorno APPDATA no fue encontrada. ¿Esto es Windows?");
				return;
			}

			// 2. Create a File object for your application's subfolder.
			File appFolder = new File(appDataPath, folderName);

			// 3. Create the directory if it does not exist.
			// use mkdirs() to create parent directories if they don't exist.
			if (!appFolder.exists()) {
				boolean created = appFolder.mkdirs();
				if (created) {
					Logger.log("Se ha creado el directorio: " + appFolder.getAbsolutePath());
				} else {
					Logger.log("No se pudo crear el directorio: " + appFolder.getAbsolutePath() + ". Es posible que ya exista.");
					return;
				}
			}

			// 4. Construct the full path to the file inside your application's folder.
			// This will create a path like: C:\Users\{username}\AppData\Roaming\MyApplication\MyTextFile.txt
			File file = new File(appFolder, fileName);

			// 5. Create the file and write content to it.
			// We use a BufferedWriter for efficient writing.
			// The try-with-resources statement ensures the writer is automatically closed.
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				writer.write(fileContent);
			}

			Logger.log("Se ha escrito en el archivo: " + file.getAbsolutePath());

		} catch (IOException e) {
			// Handle potential input/output errors (e.g., permission denied).
			Logger.log("Un error ocurrió durante la ejecución del primer inicio:");
			e.printStackTrace();
		}
	} // The Scanner is automatically closed here by the try-with-resources statement

	public static void startup() {
		if (credFileExists()) {
			String appDataPath = System.getenv("APPDATA");

			// Check if the APPDATA environment variable exists.
			if (appDataPath == null || appDataPath.isEmpty()) {
				Logger.log("La variable de entorno APPDATA no fue encontrada. ¿Esto es Windows?");
				return;
			}

			File folder = new File(appDataPath, folderName);
			File filePath = new File(folder, fileName);

			Path path = Paths.get(filePath.toString());
			try (var reader = Files.newBufferedReader(path)) {
				username = reader.readLine();
				pass = reader.readLine();

			} catch (IOException e) {
				Logger.log("Error al leer el archivo: " + e.getMessage());
				return;
			}
			Logger.log("Se han leído las credenciales con éxito.");
		}
	}
}

