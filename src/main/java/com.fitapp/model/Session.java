package com.fitapp.model;

/**
 * Christian: Merkt sich den angemeldeten Benutzer.
 * "static" = gibt es nur einmal im Programm. Jede Seite kann den Namen fragen.
 */
public class Session {

    // Christian: Name, solange sich niemand angemeldet hat.
    private static String currentUser = "Gast";

    // Christian: Beim Login setzen.
    public static void setUser(String name) {
        currentUser = name;
    }

    // Christian: Name abfragen.
    public static String getUser() {
        return currentUser;
    }
}
