package com.github.RocketSmash9000;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import static java.lang.System.exit;

public class StartupManager {
    private static final String MYSQL_SERVICE_NAME = "MySQL"; // Default MySQL service name, change if different
	// The name of your application's folder.
	public static final String FOLDER_NAME = "LoginManager";

	// The name of the file you want to create.
	public static final String FILE_NAME = "Credentials.txt";

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

		File folder = new File(appDataPath, FOLDER_NAME);
		File file = new File(folder, FILE_NAME);

		return file.exists();
	}
	public static void firstStartup() {
		if (!mySQLStartup())
			exit(1);

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
			File appFolder = new File(appDataPath, FOLDER_NAME);

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
			File file = new File(appFolder, FILE_NAME);

			// 5. Create the file and write content to it.
			// We use a BufferedWriter for efficient writing.
			// The try-with-resources statement ensures the writer is automatically closed.
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				writer.write(fileContent);
			}

			Logger.log("Se ha escrito en el archivo: " + file.getAbsolutePath());

			Cryptography.firstEncrypt(file);

		} catch (IOException e) {
			// Handle potential input/output errors (e.g., permission denied).
			Logger.log("Un error ocurrió durante la ejecución del primer inicio:");
			e.printStackTrace();
		}
	} // The Scanner is automatically closed here by the try-with-resources statement

	public static void startup() {
		if (!mySQLStartup())
			exit(1);
		if (credFileExists()) {
			String appDataPath = System.getenv("APPDATA");

			// Check if the APPDATA environment variable exists.
			if (appDataPath == null || appDataPath.isEmpty()) {
				Logger.log("La variable de entorno APPDATA no fue encontrada. ¿Esto es Windows?");
				return;
			}

			File folder = new File(appDataPath, FOLDER_NAME);
			File filePath = new File(folder, FILE_NAME);

			File archivo = Cryptography.decrypt(filePath);

			try {
				Path path = Paths.get(archivo.toString());
				try (var reader = Files.newBufferedReader(path)) {
					reader.readLine();
					username = reader.readLine();
					pass = reader.readLine();
				}
				Logger.log("Se han leído las credenciales con éxito.");

				// Delete the decrypted file after reading
				if (!Files.deleteIfExists(path)) {
					Logger.log("No se pudo eliminar el archivo desencriptado: " + path);
				}

			} catch (IOException e) {
				Logger.log("Error al leer o eliminar el archivo: " + e.getMessage());
			}
		}
	}

	/**
	 * Checks if the MySQL service is running and starts it if it's not.
     * This method requires administrator privileges to start the service.
     * @return true if the service is running or was successfully started, false otherwise
     */
    public static boolean mySQLStartup() {
        try {
            // Check if the service is already running
            Process checkProcess = Runtime.getRuntime().exec("sc query " + MYSQL_SERVICE_NAME);
            checkProcess.waitFor();
            
            // Read the output to check service status
            String output = new String(checkProcess.getInputStream().readAllBytes());
            
            if (output.contains("RUNNING")) {
                Logger.log("MySQL ya está funcionando.");
                return true;
            } else if (output.contains("STOPPED")) {
                Logger.log("MySQL está parado. Vamos a intentar iniciarlo...");
                
                // Try to start the service
                Process startProcess = Runtime.getRuntime().exec("net start " + MYSQL_SERVICE_NAME);
                int exitCode = startProcess.waitFor();
                
                if (exitCode == 0) {
                    Logger.log("MySQL se pudo ejecutar exitosamente.");
                    return true;
                } else {
                    String error = new String(startProcess.getErrorStream().readAllBytes());
                    Logger.log("No se pudo iniciar MySQL. Código de salida: " + exitCode);
                    Logger.log("Error: " + error);
					Logger.log("Por favor, inicia MySQL manualmente o contacta con el autor.");
                    return false;
                }
            } else if (output.contains("FAILED 1060")) {
                Logger.log("MySQL no está instalado o el nombre del servicio es incorrecto.");
	            Logger.log("Por favor, inicia MySQL manualmente si está instalado o contacta con el autor.");
                return false;
            } else {
                Logger.log("Ha ocurrido un problema inesperado: " + output);
	            Logger.log("Contacta con el autor para resolver el problema.");
                return false;
            }
        } catch (IOException | InterruptedException e) {
            Logger.log("Hubo un error al comprobar el mensaje de retorno: " + e.getMessage());
	        Logger.log("Por favor, contacta con el autor.");
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
}

