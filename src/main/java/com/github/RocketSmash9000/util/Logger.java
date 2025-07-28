package com.github.RocketSmash9000.util;

import com.github.RocketSmash9000.config.Config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.fusesource.jansi.Ansi.ansi;

public class Logger {
	/**
	 * {@code logLevel} indica el nivel de información que se muestra en la consola. Por defecto, {@code logLevel} vale 1.
	 * Solo se imprimirán niveles de información iguales o más altos.
	 * <li>0 - DEBUG
	 * <li>1 - INFO
	 * <li>2 - WARN
	 * <li>3 - ERROR
	 * <li>4 - FATAL -> se muestra siempre
	 */
	private static final int logLevel = Config.getInt("Logger.logLevel", 1);

	/**
	 * Imprime a nivel 0 (DEBUG) en la consola. No se imprimirá en la consola si {@code logLevel} = 0.
	 * @param string la secuencia de caracteres a imprimir
	 */
	public static void debug(String string) {
		LocalDate fecha = LocalDate.now();
		LocalTime hora = LocalTime.now();
		DateTimeFormatter formatHour = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
		DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd/MM/yy");

		String output = "DEBUG: [" + fecha.format(formatDate) + " - " + hora.format(formatHour) + "] : " + string;

		if (logLevel == 0) {
			System.out.println(ansi().fgBrightGreen().a(output).reset());
		}
		LogHelper.addToLog(output);
	}

	/**
	 * Imprime a nivel 1 (INFO) en la consola. El nivel mínimo para imprimirse en consola (según {@code logLevel}) es 1
	 * @param string la secuencia de caracteres a imprimir
	 */
	public static void info(String string) {
		LocalDate fecha = LocalDate.now();
		LocalTime hora = LocalTime.now();
		DateTimeFormatter formatHour = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
		DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd/MM/yy");

		String output = "INFO:  [" + fecha.format(formatDate) + " - " + hora.format(formatHour) + "] : " + string;

		if (logLevel <= 1) {
			System.out.println(ansi().fgBrightBlue().a(output).reset());
		}
		LogHelper.addToLog(output);
	}

	/**
	 * Imprime a nivel 2 (WARN) en la consola.
	 * @param string la secuencia de caracteres a imprimir
	 */
	public static void warn(String string) {
		LocalDate fecha = LocalDate.now();
		LocalTime hora = LocalTime.now();
		DateTimeFormatter formatHour = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
		DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd/MM/yy");

		String output = "WARN:  [" + fecha.format(formatDate) + " - " + hora.format(formatHour) + "] : " + string;

		if (logLevel <= 2) {
			System.out.println(ansi().fgRgb(255, 170, 0).a(output).reset());
		}
		LogHelper.addToLog(output);
	}

	/**
	 * Imprime a nivel 3 (ERROR) en la consola
	 * @param string la secuencia de caracteres a imprimir
	 */
	public static void error(String string) {
		LocalDate fecha = LocalDate.now();
		LocalTime hora = LocalTime.now();
		DateTimeFormatter formatHour = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
		DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd/MM/yy");

		String output = "ERROR: [" + fecha.format(formatDate) + " - " + hora.format(formatHour) + "] : " + string;

		if (logLevel <= 3) {
			System.out.println(ansi().fgRgb(255, 170, 0).a(output).reset());
		}
		LogHelper.addToLog(output);
	}

	/**
	 * Imprime a nivel 4 (FATAL) en la consola. El nivel 4 se imprime en todos los casos
	 * @param string la secuencia de caracteres a imprimir
	 */
	public static void fatal(String string) {
		LocalDate fecha = LocalDate.now();
		LocalTime hora = LocalTime.now();
		DateTimeFormatter formatHour = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
		DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd/MM/yy");

		String output = "FATAL: [" + fecha.format(formatDate) + " - " + hora.format(formatHour) + "] : " + string;
		System.out.println(ansi().fgRgb(255, 0, 0).a(output).reset());
		LogHelper.addToLog(output);
	}
}
