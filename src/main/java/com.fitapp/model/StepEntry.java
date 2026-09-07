package com.fitapp.model;

import java.time.LocalDate;

/**
 * Christian: Eine Zeile aus steps.csv.
 * Wer, welcher Tag, wie viele Schritte, welches Tagesziel.
 */
public class StepEntry {

    public String username;
    public LocalDate datum;
    public int schritte;
    public int ziel;

    // Christian: Werte beim Erzeugen setzen.
    public StepEntry(String username, LocalDate datum, int schritte, int ziel) {
        this.username = username;
        this.datum = datum;
        this.schritte = schritte;
        this.ziel = ziel;
    }
}
