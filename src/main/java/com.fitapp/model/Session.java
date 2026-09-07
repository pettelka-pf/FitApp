package com.fitapp.model;

/**
 * Haelt den aktuell eingeloggten Benutzer fest, damit die Controller wissen,
 * für wen sie Daten laden und speichern.
 *
 * Nur statische Felder: es gibt genau eine laufende Sitzung pro Programmstart.
 */
public final class Session {

    /** Kein Benutzer eingeloggt. */
    public static final int NO_USER = -1;

    private static int userId = NO_USER;
    private static String username;

    private Session() {
    }

    /** Wird nach erfolgreichem Login aufgerufen. */
    public static void login(int id, String name) {
        userId = id;
        username = name;
    }

    /** Wird beim Logout aufgerufen, damit keine Daten hängen bleiben. */
    public static void logout() {
        userId = NO_USER;
        username = null;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getUsername() {
        return username;
    }

    public static boolean isLoggedIn() {
        return userId != NO_USER;
    }
}