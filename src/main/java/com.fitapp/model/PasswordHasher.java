package com.fitapp.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hashes passwords with SHA-256 so that no plain text is written to the database.
 *
 * Utility class: only static methods, no instances.
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "SHA-256";

    /** Utility-Klasse, wird nie instanziiert. */
    private PasswordHasher() {
    }

    /**
     * Hashes a password and returns it as a lowercase hex string.
     * The result is always 64 characters long.
     *
     * @param password the plain text password, must not be null
     * @return the SHA-256 hash as hex
     */
    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("password must not be null");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 ist in jeder JVM vorhanden, dieser Fall tritt nie ein.
            throw new IllegalStateException(ALGORITHM + " ist nicht verfuegbar", e);
        }
    }

    /**
     * Checks a typed password against the hash stored in the database.
     *
     * @param plainPassword what the user typed into the login form
     * @param storedHash    the value from the users.password column
     * @return true if they match
     */
    public static boolean matches(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        return hash(plainPassword).equals(storedHash);
    }
}