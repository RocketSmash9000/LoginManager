package com.github.RocketSmash9000;

import static java.lang.Character.isDigit;
import static java.lang.Character.isLetter;

public class Checker {
	public static boolean validDNI(String dni) {
		if (dni.length() != 9)
			return false;
		for (int i = 0; i < dni.length(); i++){
			if ((i < 8) && !isDigit(dni.charAt(i)))
				return false;
			if (i == 8 && isLetter(dni.charAt(i)))
				return true;
		}
		return false;
	}
}
