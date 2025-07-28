package com.github.RocketSmash9000.config;

import com.github.RocketSmash9000.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Clase genérica para manejar la configuración de la aplicación.
 * Proporciona métodos estáticos para leer y escribir propiedades de configuración
 * en un archivo ubicado en el directorio de configuración del usuario.
 */
public class Config {
    public static final String CONFIG_DIR = System.getenv("APPDATA") + "\\AnyManager";
    private static final String CONFIG_FILE = CONFIG_DIR + "\\user.cfg";
    private static final Properties props = new Properties();
    private static volatile boolean configuracionCargada = false;
    private static final Object lock = new Object();

    // Inicialización perezosa
    private static void asegurarConfiguracionCargada() {
        if (!configuracionCargada) {
            synchronized (lock) {
                if (!configuracionCargada) {
                    cargarConfiguracion();
                    configuracionCargada = true;
                }
            }
        }
    }

    /**
     * Carga la configuración desde el archivo, o crea uno vacío si no existe.
     */
    private static void cargarConfiguracion() {
        File configFile = new File(CONFIG_FILE);

        try {
            // Crear directorio si no existe
            Files.createDirectories(Paths.get(CONFIG_DIR));

            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                    //Logger.debug("Configuración cargada correctamente desde " + CONFIG_FILE);
                }
            } else {
                // Guardar archivo de configuración vacío
                guardarConfiguracion();
                //Logger.debug("Archivo de configuración creado en " + CONFIG_FILE);
            }
        } catch (IOException e) {
            Logger.error("Error al cargar la configuración: " + e);
        }
    }

    /**
     * Guarda la configuración actual en el archivo.
     */
    private static void guardarConfiguracion() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Configuración de la aplicación");
            Logger.debug("Configuración guardada correctamente en " + CONFIG_FILE);
        } catch (IOException e) {
            Logger.error("Error al guardar la configuración: " + e);
        }
    }

    /**
     * Obtiene un valor de configuración como String.
     * Si la clave no existe, guarda el valor por defecto en la configuración.
     * 
     * @param key La clave de la propiedad
     * @param defaultValue Valor por defecto si la propiedad no existe
     * @return El valor de la propiedad o el valor por defecto
     */
    public static String getString(String key, String defaultValue) {
        asegurarConfiguracionCargada();
        
        // Si la clave no existe, guardar el valor por defecto
        if (!props.containsKey(key) && defaultValue != null) {
            props.setProperty(key, defaultValue);
            // Usar un nuevo hilo para guardar y no bloquear
            new Thread(Config::guardarConfiguracion, "Config-Save-Thread").start();
        }
        
        return props.getProperty(key, defaultValue);
    }

    /**
     * Obtiene un valor de configuración como entero.
     * Si la clave no existe, guarda el valor por defecto en la configuración.
     * 
     * @param key La clave de la propiedad
     * @param defaultValue Valor por defecto si la propiedad no existe o no es un entero válido
     * @return El valor de la propiedad como entero o el valor por defecto
     */
    public static int getInt(String key, int defaultValue) {
        asegurarConfiguracionCargada();
        String stringValue = props.getProperty(key);
        
        // Si la clave no existe, guardar el valor por defecto
        if (stringValue == null) {
            setInt(key, defaultValue);
            return defaultValue;
        }
        
        // Si la clave existe pero el valor no es un número, intentar convertirlo
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            Logger.error(String.format("Valor no válido para la propiedad '%s': %s. Usando valor por defecto: %d", 
                key, stringValue, defaultValue));
            // Corregir el valor inválido con el valor por defecto
            setInt(key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Establece un valor de configuración.
     *
     * @param key La clave de la propiedad
     * @param value El valor a establecer
     */
    public static void set(String key, String value) {
        asegurarConfiguracionCargada();
        String oldValue = props.getProperty(key);
        if (value == null) {
            props.remove(key);
        } else {
            props.setProperty(key, value);
        }
        // Solo guardar si el valor ha cambiado
        if (oldValue == null || !oldValue.equals(value)) {
            guardarConfiguracion();
        }
    }

    /**
     * Establece un valor de configuración entero.
     *
     * @param key La clave de la propiedad
     * @param value El valor entero a establecer
     */
    public static void setInt(String key, int value) {
        set(key, String.valueOf(value));
    }

    /**
     * Elimina una propiedad de configuración.
     *
     * @param key La clave de la propiedad a eliminar
     */
    public static void remove(String key) {
        asegurarConfiguracionCargada();
        if (props.containsKey(key)) {
            props.remove(key);
            guardarConfiguracion();
        }
    }

    /**
     * Limpia toda la configuración.
     */
    public static void clear() {
        asegurarConfiguracionCargada();
        if (!props.isEmpty()) {
            props.clear();
            guardarConfiguracion();
        }
    }
}
