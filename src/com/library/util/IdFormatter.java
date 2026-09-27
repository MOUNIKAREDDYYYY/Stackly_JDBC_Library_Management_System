package com.library.util;

/**
 * Utility class to format and parse entity IDs into user-friendly strings.
 * Examples:
 *   Book ID: 1 -> B001
 *   Member ID: 1 -> M001
 *   Request ID: 1 -> R001
 *   Issue ID: 1 -> I001
 */
public class IdFormatter {

    public static String formatBookId(int id) {
        return String.format("B%03d", id);
    }

    public static String formatMemberId(int id) {
        return String.format("M%03d", id);
    }

    public static String formatRequestId(int id) {
        return String.format("R%03d", id);
    }

    public static String formatIssueId(int id) {
        return String.format("I%03d", id);
    }

    /**
     * Flexible ID parser: Accepts inputs like "B001", "b1", "1", "I007", "7"
     * and extracts the raw integer ID.
     */
    public static int parseId(String input) throws NumberFormatException {
        if (input == null || input.isBlank()) {
            throw new NumberFormatException("Empty ID string.");
        }
        String clean = input.trim().replaceAll("^[a-zA-Z]+", "");
        return Integer.parseInt(clean);
    }
}
