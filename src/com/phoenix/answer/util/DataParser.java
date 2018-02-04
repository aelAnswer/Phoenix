package com.phoenix.answer.util;

public final class DataParser {

	private DataParser() {}
	
	public static double parseDouble(String t) {
		try {
			return Double.parseDouble(t);
		} catch (NumberFormatException nfe) {
			return 0;
		}
	}

	public static int parseInteger(String t) {
		try {
			return Integer.parseInt(t);
		} catch (NumberFormatException nfe) {
			return 0;
		}
	}
}
