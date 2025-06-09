package com.eto2112.mmoitemsrepair.utils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class MessageUtil {

    /**
     * Apply color codes to a string using & as the color code prefix
     */
    public static String colorize(String message) {
        if (message == null) {
            return null;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Apply color codes to a list of strings
     */
    public static List<String> colorizeList(List<String> messages) {
        if (messages == null) {
            return null;
        }

        List<String> colorized = new ArrayList<>();
        for (String message : messages) {
            colorized.add(colorize(message));
        }
        return colorized;
    }

    /**
     * Strip all color codes from a string
     */
    public static String stripColor(String message) {
        if (message == null) {
            return null;
        }
        return ChatColor.stripColor(message);
    }

    /**
     * Format a message with placeholders
     */
    public static String formatMessage(String message, String... replacements) {
        if (message == null) {
            return null;
        }

        String formatted = message;
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                formatted = formatted.replace(replacements[i], replacements[i + 1]);
            }
        }
        return colorize(formatted);
    }

    /**
     * Format a list of messages with placeholders
     */
    public static List<String> formatMessages(List<String> messages, String... replacements) {
        if (messages == null) {
            return null;
        }

        List<String> formatted = new ArrayList<>();
        for (String message : messages) {
            formatted.add(formatMessage(message, replacements));
        }
        return formatted;
    }

    /**
     * Center text with a specific character and length
     */
    public static String centerText(String text, char centerChar, int length) {
        if (text == null) {
            text = "";
        }

        if (text.length() >= length) {
            return text;
        }

        int padding = (length - text.length()) / 2;
        StringBuilder sb = new StringBuilder();

        // Add left padding
        for (int i = 0; i < padding; i++) {
            sb.append(centerChar);
        }

        sb.append(text);

        // Add right padding
        while (sb.length() < length) {
            sb.append(centerChar);
        }

        return sb.toString();
    }

    /**
     * Create a separator line
     */
    public static String createSeparator(char character, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(character);
        }
        return sb.toString();
    }

    /**
     * Check if a string contains color codes
     */
    public static boolean hasColorCodes(String message) {
        if (message == null) {
            return false;
        }
        return message.contains("&") || message.contains("§");
    }

    /**
     * Get the length of a string without color codes
     */
    public static int getStringLength(String message) {
        if (message == null) {
            return 0;
        }
        return stripColor(message).length();
    }

    /**
     * Truncate a string to a specific length, preserving color codes
     */
    public static String truncate(String message, int maxLength) {
        if (message == null) {
            return null;
        }

        String stripped = stripColor(message);
        if (stripped.length() <= maxLength) {
            return message;
        }

        return stripped.substring(0, maxLength) + "...";
    }

    /**
     * Format number with commas
     */
    public static String formatNumber(double number) {
        return String.format("%,.0f", number);
    }

    /**
     * Format percentage
     */
    public static String formatPercentage(double percentage) {
        return String.format("%.1f%%", percentage * 100);
    }
}