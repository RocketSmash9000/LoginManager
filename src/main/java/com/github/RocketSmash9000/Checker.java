package com.github.RocketSmash9000;

import static java.lang.Character.isDigit;
import static java.lang.Character.isLetter;

public class Checker {
	/**
	 * Checks if a given DNI is valid or not. Only performs a basic check.
	 * @param dni the DNI to check.
	 * @return true if the DNI is valid, false if it isn't.
	 */
	public static boolean validDNI(String dni) {
		if (dni.length() != 9)
			return false;
		// Foreigner DNIs also exist
		for (int i = 1; i < dni.length(); i++){
			if ((i < 8) && !isDigit(dni.charAt(i)))
				return false;
			if (i == 8 && isLetter(dni.charAt(i)))
				return true;
		}
		return false;
	}
}
