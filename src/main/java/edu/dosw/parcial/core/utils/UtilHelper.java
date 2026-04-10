package edu.dosw.parcial.core.utils;

/**
 * Utility classes for common operations
 */
public class UtilHelper {

	private UtilHelper() {
	}

	public static String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase();
	}

	public static boolean isInstitutionalEmail(String email) {
		String normalized = normalizeEmail(email);
		if (normalized == null || !normalized.contains("@")) {
			return false;
		}
		String[] parts = normalized.split("@", 2);
		return parts.length == 2 && parts[1].contains(".edu");
	}
}
