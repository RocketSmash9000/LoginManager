package com.github.RocketSmash9000; // Make sure you have the correct package declaration

import com.github.RocketSmash9000.config.Config;
import com.github.RocketSmash9000.util.Logger;
import org.fusesource.jansi.AnsiConsole;

import java.io.Console;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.exit;
import static spark.Spark.*;

public class WebServer {
	static boolean credFileExists;
	private static boolean logLevelChanged = false;

	/**
	 * Checks if the program is running in a console and opens one if needed.
	 * @return true if running in a console or if a new console was successfully opened, false otherwise
	 */
	@SuppressWarnings({"t"})
	private static boolean ensureConsole() {
		// Try to get the console
		Console console = System.console();

		// If we have a console, we're good to go
		if (console != null) {
			return true;
		}

		// No console, try to open one
		try {
			String javaHome = System.getProperty("java.home");
			String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
			String classpath = System.getProperty("java.class.path");
			String className = WebServer.class.getCanonicalName();

			// Build the command to start a new console with the same classpath
			List<String> command = new ArrayList<>();
			command.add("cmd");
			command.add("/c");
			command.add("start");
			command.add("\"QueryManager\"");
			command.add(javaBin);
			command.add("-cp");
			command.add(classpath);
			command.add(className);

			// Add original arguments
			command.addAll(Arrays.asList(System.getProperty("sun.java.command").split("\\s+")));

			// Start the new process
			new ProcessBuilder()
					.command(command)
					.inheritIO()
					.start();

			return false; // Original process should exit
		} catch (Exception e) {
			// If we can't open a new console, at least show an error dialog
			System.err.println("No se pudo abrir una consola: " + e.getMessage());
			return false;
		}
	}

	@SuppressWarnings({"t", "D"})
	public static void main(String[] args) {
		// Intenta que la consola use caracteres especiales
		System.setOut(new java.io.PrintStream(System.out, true, StandardCharsets.UTF_8));
		System.setProperty("jansi.passthrough", "true");
		AnsiConsole.systemInstall();
		Config.set("Storage.baseDir",System.getenv("APPDATA") + "\\AnyManager");

		// Guarda en la configuración el sitio donde se ha ejecutado LoginManager por última vez.
		String userDirectory = new File("").getAbsolutePath();
		Config.set("System.loginLast", userDirectory);

		final int LOG_LEVEL = Config.getInt("Logger.logLevel", 1);
		boolean debug = false;

		for (String v : args) {
			// Muestra ayuda y comandos.
			switch (v) {
				case "-h", "--help" -> {
					System.out.println("""
							Modo de uso: java -jar QueryManager.jar [argumentos]
							
							Argumentos opcionales:
							-v, --version -> Muestra la versión.
							-p, --port    -> Muestra el puerto por el que conectarse a la interfaz web.
							-a, --all     -> Muestra toda la información posible al ejecutar QueryManager.
							-n, -none     -> Solo muestra errores fatales en la consola al ejecutar.
							-h, --help    -> Muestra esto en la consola.
							-d, --debug   -> Cambia el nivel de logs a DEBUG e impide la aparición de consolas. Sin uso fuera de un IDE
							--erase       -> Borra las credenciales. Necesita autenticación para borrar.
							--delete-all  -> Borra cualquier presencia que las herramientas AnyManager tuvieron en el disco duro.
											 Es básicamente el último recurso si nada va bien. Necesita autenticación.
							
							Dejar opciones en blanco para ejecutar en modo normal.
							""");
					exit(0);
				}

				// Obtiene la versión del programa
				case "-v", "--version" -> {
					String version = Version.getVersion();
					System.out.println("Versión: " + (version != null ? version : "Desarrollo"));
					exit(0);
				}

				// Imprime todos los logs durante una sola ejecución
				case "-a", "--all" -> {
					Config.setInt("Logger.logLevel", 0);
					logLevelChanged = true;
				}

				// Durante una sola ejecución, solo imprimirá logs de nivel 4
				case "-n", "--none" -> {
					Config.setInt("Logger.logLevel", 4);
					logLevelChanged = true;
				}

				case "-d", "--debug" -> {
					Config.setInt("Logger.logLevel", 0);
					logLevelChanged = true;
					debug = true;
				}
			}
		}

		// If we're not debugging...
		if (!debug) {
			// Ensure we're running in a console
			if (!ensureConsole()) {
				exit(0); // Exit the original process if we're starting a new console
			}
		}

		System.out.println("""
                dP                          oo          8888ba.88ba                                                       \s
                88                                      88  `8b  `8b                                                      \s
                88        .d8888b. .d8888b. dP 88d888b. 88   88   88 .d8888b. 88d888b. .d8888b. .d8888b. .d8888b. 88d888b.\s
                88        88'  `88 88'  `88 88 88'  `88 88   88   88 88'  `88 88'  `88 88'  `88 88'  `88 88ooood8 88'  `88\s
                88        88.  .88 88.  .88 88 88    88 88   88   88 88.  .88 88    88 88.  .88 88.  .88 88.  ... 88      \s
                88888888P `88888P' `8888P88 dP dP    dP dP   dP   dP `88888P8 dP    dP `88888P8 `8888P88 `88888P' dP      \s
                                        .88                                                          .88                  \s
                                    d8888P                                                       d8888P                   \s""");
		System.out.println("\n                            Un programa creado por RocketSmash9000\n");

		//* Configurar el apagado limpio
		//* En caso de necesitar devolver algo a su estado original, usar este bloque
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (logLevelChanged) {
				Logger.debug("Devolviendo el nivel de log al original...");
				Config.setInt("Logger.logLevel", LOG_LEVEL);
			}
		}));

		credFileExists = StartupManager.credFileExists();
		if (!credFileExists) {
			StartupManager.firstStartup();
		} else StartupManager.startup();

		int resultado = Conexión.creaDB();
		switch (resultado){
			case 0:
				// no ha habido ningún error y la tabla ya está creada.
				break;
			case 1:
				// La tabla no estaba creada ni la base de datos.
				Conexión.creaDB();
			default:
				break;
		}

		// Configure Spark
		port(2048);
		staticFiles.location("/public"); // Serve static files from resources/public
		staticFiles.expireTime(600); // Cache static files for 10 minutes

		// Add a route to serve the mis-registros.html file
		get("/mis-registros", (req, res) -> {
			res.redirect("/mis-registros.html");
			return null;
		});

		// Add a route to serve the root path
		get("/", (req, res) -> {
			res.redirect("/index.html");
			return null;
		});

		// Enable CORS
		options("/*", (request, response) -> {
			String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
			if (accessControlRequestHeaders != null) {
				response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
			}
			String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
			if (accessControlRequestMethod != null) {
				response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
			}
			return "OK";
		});

		before((request, response) -> {
			response.header("Access-Control-Allow-Origin", "*");
			response.header("Access-Control-Request-Method", "*");
			response.type("text/plain; charset=utf-8");
		});

		// Handle form submission for "entrada"
		post("/entrada", (req, res) -> {
			String dni = req.queryParams("dni");
			String password = req.queryParams("password");
			String observaciones = req.queryParams("observaciones");

			if (dni == null || dni.trim().isEmpty()) {
				res.status(400);
				return "Error: DNI no puede estar vacío";
			} else if (!Checker.validDNI(dni)) {
				res.status(400);
				return "Error: DNI no es válido";
			}

			// Check if this is a new user registration
			if (password != null && !password.isEmpty()) {
				// Check if user already exists
				int userStatus = Login.validarUsuario(dni, password);
				switch (userStatus) {
					case 1:
						res.status(400);
						return "La contraseña es incorrecta.";
					case 2:
						if (!Login.añadirUsuario(dni, password)) {
							res.status(500);
							return "Error al registrar el nuevo usuario";
						}
					case 3:
						res.status(500);
						return "No se pudieron leer las credenciales";
				}
			}

			int result = Conexión.insertarEntrada(dni.trim(), observaciones != null ? observaciones.trim() : null);
			if (result == 0) {
				return "Entrada registrada correctamente para DNI: " + dni;
			} else {
				res.status(500);
				return "Error al registrar la entrada";
			}
		});

		// Handle form submission for "salida"
		post("/salida", (req, res) -> {
			String dni = req.queryParams("dni");
			String password = req.queryParams("password");
			String observaciones = req.queryParams("observaciones");

			if (dni == null || dni.trim().isEmpty()) {
				res.status(400);
				return "Error: DNI no puede estar vacío";
			} else if (!Checker.validDNI(dni)) {
				res.status(400);
				return "Error: DNI no es válido";
			}

			// Check if this is a new user registration
			if (password != null && !password.isEmpty()) {
				// Check if user already exists
				int userStatus = Login.validarUsuario(dni, password);
				switch (userStatus) {
					case 1:
						res.status(400);
						return "La contraseña es incorrecta.";
					case 2:
						if (!Login.añadirUsuario(dni, password)) {
							res.status(500);
							return "Error al registrar el nuevo usuario";
						}
					case 3:
						res.status(500);
						return "No se pudieron leer las credenciales";
				}
			}

			int result = Conexión.insertarSalida(dni.trim(), observaciones != null ? observaciones.trim() : null);
			if (result == 0) {
				return "Salida registrada correctamente para DNI: " + dni;
			} else {
				res.status(500);
				return "Error al registrar la salida";
			}
		});

		// Endpoint to check if the user is valid and all that
		post("/check-user", (req, res) -> {
			String dni = req.queryParams("dni");
			if (dni == null || dni.trim().isEmpty()) {
				res.status(400);
				return "{\"error\":\"DNI requerido\"}";
			}

			// Check if user exists using your existing Login.validarUsuario method
			int userStatus = Login.validarUsuario(dni, "dummy_password");
			boolean isNewUser = (userStatus == 2); // 2 means user doesn't exist

			return "{\"isNewUser\":" + isNewUser + "}";
		});

		// API endpoint to get user records
		get("/api/mis-registros", (req, res) -> {
			String dni = req.queryParams("dni");
			String password = req.queryParams("password");

			if (dni == null || dni.trim().isEmpty() || password == null || password.trim().isEmpty()) {
				res.status(400);
				return "{\"error\":\"Se requieren DNI y contraseña\"}";
			}

			// Validate user credentials
			int userStatus = Login.validarUsuario(dni, password);
			if (userStatus != 0) { // 0 means valid credentials
				res.status(401);
				return "{\"error\":\"Credenciales inválidas\"}";
			}

			// Get user records
			String records = Conexión.obtenerRegistrosUsuario(dni);
			res.type("application/json");
			return records;
		});
	}
}

class Version {
	public static String getVersion() {
		return Version.class.getPackage().getImplementationVersion();
	}
}