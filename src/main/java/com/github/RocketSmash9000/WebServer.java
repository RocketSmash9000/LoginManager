package com.github.RocketSmash9000; // Make sure you have the correct package declaration

import static spark.Spark.*;

public class WebServer {
    static boolean credFileExists;
    @SuppressWarnings("t")
    public static void main(String[] args) {
        System.out.println();
        System.out.println("            LoginManager");
        System.out.println("Un programa creado por RocketSmash9000");
        System.out.println();
        System.out.println();

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
            if (dni == null || dni.trim().isEmpty()) {
                res.status(400);
                return "Error: DNI no puede estar vacío";
            } else if (!Checker.validDNI(dni)) {
                res.status(400);
                return "Error: DNI no es válido";
            }
            
            int result = Conexión.insertarEntrada(dni.trim());
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
            if (dni == null || dni.trim().isEmpty()) {
                res.status(400);
                return "Error: DNI no puede estar vacío";
            } else if (!Checker.validDNI(dni)) {
                res.status(400);
                return "Error: DNI no es válido";
            }

            int result = Conexión.insertarSalida(dni.trim());
            if (result == 0) {
                return "Salida registrada correctamente para DNI: " + dni;
            } else {
                res.status(500);
                return "Error al registrar la salida";
            }
        });

        // Root route - serve the index page
        get("/", (req, res) -> {
            res.redirect("/index.html");
            return null;
        });
    }
}

