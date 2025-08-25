package com.github.RocketSmash9000;

import com.github.RocketSmash9000.config.Config;
import com.github.RocketSmash9000.util.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

import static java.lang.System.exit;

public class StartupManager {
    private static String MYSQL_SERVICE_NAME = "MySQL"; // Default MySQL service name, change if different
	// The name of the application's folder.
	public static String folderName;

	// The name of the file to create.
	public static final String FILE_NAME = "Credentials.txt";

	public static String username;
	public static String pass;
	public static boolean noCheck = false;

	public static boolean credFileExists(){
		folderName = Config.configDir;

		File folder = new File(folderName);
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
			// Create a File object for the application's subfolder.
			File appFolder = new File(folderName);

			// Create the directory if it does not exist.
			// use mkdirs() to create parent directories if they don't exist.
			if (!appFolder.exists()) {
				boolean created = appFolder.mkdirs();
				if (created) {
					Logger.debug("Se ha creado el directorio: " + appFolder.getAbsolutePath());
				} else {
					Logger.debug("No se pudo crear el directorio: " + appFolder.getAbsolutePath() + ". Es posible que ya exista.");
					return;
				}
			}

			// Construct the full path to the file inside the application's folder.
			// This will create a path like: C:\Users\{username}\AppData\Roaming\MyApplication\MyTextFile.txt
			File file = new File(appFolder, FILE_NAME);

			// Create the file and write content to it.
			// We use a BufferedWriter for efficient writing.
			// The try-with-resources statement ensures the writer is automatically closed.
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				writer.write(fileContent);
			}

			Logger.debug("Se ha escrito en el archivo: " + file.getAbsolutePath());

			Cryptography.firstEncrypt(file);

		} catch (IOException e) {
			// Handle potential input/output errors (e.g., permission denied).
			Logger.error("Un error ocurrió durante la ejecución del primer inicio:");
			Logger.error(Arrays.toString(e.getStackTrace()));
		}
	} // The Scanner is automatically closed here by the try-with-resources statement

	public static void startup() {
		folderName = Config.configDir;

		if (!mySQLStartup())
			exit(1);
		if (credFileExists()) {

			File folder = new File(folderName);
			File filePath = new File(folder, FILE_NAME);

			File archivo = Cryptography.decrypt(filePath);

			try {
				Path path = Paths.get(archivo.toString());
				try (var reader = Files.newBufferedReader(path)) {
					reader.readLine();
					username = reader.readLine();
					pass = reader.readLine();
				}
				Logger.debug("Se han leído las credenciales con éxito.");

				// Delete the decrypted file after reading
				if (!Files.deleteIfExists(path)) {
					Logger.warn("No se pudo eliminar el archivo desencriptado: " + path);
				}

			} catch (IOException e) {
				Logger.error("Error al leer o eliminar el archivo: " + e.getMessage());
				Logger.debug(Arrays.toString(e.getStackTrace()));
			}
		}
	}

	/**
	 * Checks if the MySQL service is running and starts it if it's not.
     * This method requires administrator privileges to start the service.
     * @return true if the service is running or was successfully started, false otherwise
     */
    public static boolean mySQLStartup() {
		if (noCheck) {
			Logger.info("No vamos a comprobar si MySQL funciona. Pueden ocurrir errores.");
			return true;
		}
        try {
            // Check if the service is already running
            Process checkProcess = Runtime.getRuntime().exec("sc query " + MYSQL_SERVICE_NAME);
            checkProcess.waitFor();
            
            // Read the output to check service status
            String output = new String(checkProcess.getInputStream().readAllBytes());
            
            if (output.contains("RUNNING")) {
                Logger.debug("MySQL ya está funcionando.");
                return true;
            } else if (output.contains("STOPPED")) {
                Logger.info("MySQL está parado. Vamos a intentar iniciarlo...");
                
                // Try to start the service
                Process startProcess = Runtime.getRuntime().exec("net start " + MYSQL_SERVICE_NAME);
                int exitCode = startProcess.waitFor();
                
                if (exitCode == 0) {
                    Logger.info("MySQL se pudo ejecutar exitosamente.");
                    return true;
                } else {
                    String error = new String(startProcess.getErrorStream().readAllBytes());
                    Logger.error("No se pudo iniciar MySQL. Código de salida: " + exitCode);
                    Logger.error("Error: " + error);
					Logger.info("Por favor, inicia MySQL manualmente o contacta con el autor.");
                    return false;
                }
            } else if (output.contains("FAILED 1060")) {
                Logger.warn("MySQL no está instalado o el nombre del servicio es incorrecto.");
				Logger.info("Vamos a probar con otro nombre");

				MYSQL_SERVICE_NAME = "MySQL80";
				// A veces MySQL está instalado como MySQL80, así que comprobamos eso también por si acaso
	            try {
		            // Check if the service is already running
		            checkProcess = Runtime.getRuntime().exec("sc query " + MYSQL_SERVICE_NAME);
		            checkProcess.waitFor();

		            // Read the output to check service status
		            output = new String(checkProcess.getInputStream().readAllBytes());

		            if (output.contains("RUNNING")) {
			            Logger.debug("MySQL ya está funcionando.");
			            return true;
		            } else if (output.contains("STOPPED")) {
			            Logger.info("MySQL está parado. Vamos a intentar iniciarlo...");

			            // Try to start the service
			            Process startProcess = Runtime.getRuntime().exec("net start " + MYSQL_SERVICE_NAME);
			            int exitCode = startProcess.waitFor();

			            if (exitCode == 0) {
				            Logger.info("MySQL se pudo ejecutar exitosamente.");
				            return true;
			            } else {
				            String error = new String(startProcess.getErrorStream().readAllBytes());
				            Logger.error("No se pudo iniciar MySQL. Código de salida: " + exitCode);
				            Logger.error("Error: " + error);
				            Logger.info("Por favor, inicia MySQL manualmente o contacta con el autor.");
				            return false;
			            }
		            } else if (output.contains("FAILED 1060")) {
			            Logger.warn("MySQL no está instalado o el nombre del servicio es incorrecto.");
			            Logger.info("Por favor, inicia MySQL manualmente si está instalado o contacta con el autor.");
			            return false;
		            } else {
			            Logger.error("Ha ocurrido un problema inesperado: " + output);
			            Logger.info("Contacta con el autor para resolver el problema.");
			            return false;
		            }
	            } catch (IOException | InterruptedException e) {
		            Logger.error("Hubo un error al comprobar el mensaje de retorno: " + e.getMessage());
		            Logger.debug(Arrays.toString(e.getStackTrace()));
		            Logger.info("Por favor, contacta con el autor.");
		            if (e instanceof InterruptedException) {
			            Thread.currentThread().interrupt();
		            }
		            return false;
	            }
            } else {
                Logger.error("Ha ocurrido un problema inesperado: " + output);
	            Logger.info("Contacta con el autor para resolver el problema.");
                return false;
            }
        } catch (IOException | InterruptedException e) {
            Logger.error("Hubo un error al comprobar el mensaje de retorno: " + e.getMessage());
	        Logger.debug(Arrays.toString(e.getStackTrace()));
	        Logger.info("Por favor, contacta con el autor.");
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
}

