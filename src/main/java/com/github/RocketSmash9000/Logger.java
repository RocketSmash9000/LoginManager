package com.github.RocketSmash9000;

import java.time.LocalDate;
import java.time.LocalTime;

public class Logger {
	public static void log(String string) {
		LocalDate fecha = LocalDate.now();       // Fecha actual
		LocalTime hora = LocalTime.now();

		String output = " [" + fecha + " - " + hora + "] >>> " + string;
		System.out.println(output);
	}
}
