package com.github.RocketSmash9000; // Make sure you have the correct package declaration

import static spark.Spark.*;

public class WebServer {
	static boolean credFileExists;
	@SuppressWarnings("t")
	public static void main(String[] args) {
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
