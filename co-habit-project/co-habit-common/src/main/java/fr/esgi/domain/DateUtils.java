package fr.esgi.domain;

import fr.esgi.domain.exception.TechnicalException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for date-related operations and conversions
 */
public class DateUtils {

    public static final  String            DEFAULT_DATE_PATTERN       = "yyyy-MM-dd";
    public static final  String            DEFAULT_DATETIME_PATTERN   = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final DateTimeFormatter DEFAULT_FORMATTER          = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);
    private static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN);

    private DateUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Converts a string to LocalDate using the default pattern (yyyy-MM-dd)
     *
     * @param dateString the date string to convert
     * @return the converted LocalDate
     * @throws TechnicalException if the string cannot be parsed
     */
    public static LocalDate stringToLocalDate(String dateString) throws
                                                                 TechnicalException {
        return stringToLocalDate(dateString, DEFAULT_DATE_PATTERN);
    }

    /**
     * Converts a string to LocalDate using a specified pattern
     *
     * @param dateString the date string to convert
     * @param pattern    the date pattern to use for parsing
     * @return the converted LocalDate
     * @throws TechnicalException if the string cannot be parsed
     */
    public static LocalDate stringToLocalDate(String dateString, String pattern) throws
                                                                                 TechnicalException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return LocalDate.parse(dateString, formatter);
        } catch (DateTimeParseException e) {
            throw new TechnicalException("Failed to parse date: " + dateString + " with pattern: " + pattern, e);
        }
    }

    /**
     * Converts a LocalDate to string using the default pattern (yyyy-MM-dd)
     *
     * @param date the LocalDate to convert
     * @return the formatted date string
     */
    public static String localDateToString(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DEFAULT_FORMATTER);
    }

    /**
     * Converts a LocalDate to string using a specified pattern
     *
     * @param date    the LocalDate to convert
     * @param pattern the date pattern to use for formatting
     * @return the formatted date string
     */
    public static String localDateToString(LocalDate date, String pattern) {
        if (date == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }

    /**
     * Safely converts a string to LocalDate, returning null if the input is null or empty
     *
     * @param dateString the date string to convert
     * @return the converted LocalDate or null
     */
    public static LocalDate safeStringToLocalDate(String dateString) {
        if (dateString == null || dateString.trim()
                                            .isEmpty()) {
            return null;
        }
        try {
            return stringToLocalDate(dateString);
        } catch (TechnicalException e) {
            return null;
        }
    }

    /**
     * Converts a LocalDateTime to ISO 8601 string format
     *
     * @param dateTime the LocalDateTime to convert
     * @return the formatted date-time string
     */
    public static String localDateTimeToString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toString();
    }

    /**
     * Converts a LocalDateTime to string using a specified pattern
     *
     * @param dateTime the LocalDateTime to convert
     * @param pattern  the date-time pattern to use for formatting
     * @return the formatted date-time string
     */
    public static String localDateTimeToString(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }
}
