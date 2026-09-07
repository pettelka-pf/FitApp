package com.fitapp.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Christian: Liest und schreibt die Datei steps.csv.
 * Eine Zeile: username,datum,schritte,ziel
 * Beispiel:   Rene,2026-09-01,3500,10000
 * Die Datei liegt neben users.csv in src/main/resources/data/.
 */
public class StepCsv {

    // Christian: Pfad zur Datei.
    private static final File DATEI =
            new File("src/main/resources/data/steps.csv");

    // Christian: Beim ersten Benutzen Beispieldaten anlegen, falls die Datei fehlt.
    static {
        if (!DATEI.exists()) {
            seedDemo();
        }
    }

    // ----- Schreiben -----

    // Christian: Eine Zeile hinten anhaengen.
    public static void append(StepEntry e) {
        // Christian: "true" = anhaengen statt ueberschreiben.
        try (BufferedWriter w = new BufferedWriter(new FileWriter(DATEI, true))) {
            w.write(e.username + "," + e.datum + "," + e.schritte + "," + e.ziel);
            w.newLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Christian: Tag speichern. Alter Eintrag vom selben Tag wird ersetzt.
    public static void saveOrReplace(StepEntry neu) {
        List<StepEntry> alle = readAll();
        // Christian: alten Eintrag (gleicher Benutzer, gleicher Tag) entfernen.
        alle.removeIf(e -> e.username.equals(neu.username)
                && e.datum.equals(neu.datum));
        alle.add(neu);
        schreibeAlle(alle);
    }

    // Christian: Eintrag fuer einen Tag loeschen.
    public static void delete(String username, LocalDate datum) {
        List<StepEntry> alle = readAll();
        alle.removeIf(e -> e.username.equals(username) && e.datum.equals(datum));
        schreibeAlle(alle);
    }

    // Christian: Schritte von einem Benutzer an einem Tag. 0 = nichts gespeichert.
    public static int schritteFuer(String username, LocalDate datum) {
        int summe = 0;
        for (StepEntry e : readAll()) {
            if (e.username.equals(username) && e.datum.equals(datum)) {
                summe += e.schritte;
            }
        }
        return summe;
    }

    // Christian: Ganze Liste neu schreiben, sortiert nach Datum.
    private static void schreibeAlle(List<StepEntry> liste) {
        liste.sort((a, b) -> a.datum.compareTo(b.datum));
        try (BufferedWriter w = new BufferedWriter(new FileWriter(DATEI, false))) {
            for (StepEntry e : liste) {
                w.write(e.username + "," + e.datum + "," + e.schritte + "," + e.ziel);
                w.newLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ----- Lesen -----

    // Christian: Alle Zeilen der Datei in eine Liste lesen.
    public static List<StepEntry> readAll() {
        List<StepEntry> liste = new ArrayList<>();

        // Christian: keine Datei -> leere Liste.
        if (!DATEI.exists()) {
            return liste;
        }

        try (BufferedReader r = new BufferedReader(new FileReader(DATEI))) {
            String zeile;
            while ((zeile = r.readLine()) != null) {

                // Christian: leere Zeile ueberspringen.
                if (zeile.isBlank()) {
                    continue;
                }

                // Christian: an den Kommas trennen: username, datum, schritte, ziel.
                String[] teile = zeile.split(",");

                // Christian: kaputte Zeile ueberspringen.
                if (teile.length < 4) {
                    continue;
                }

                liste.add(new StepEntry(
                        teile[0].trim(),
                        LocalDate.parse(teile[1].trim()),
                        Integer.parseInt(teile[2].trim()),
                        Integer.parseInt(teile[3].trim())));
            }
        } catch (IOException | RuntimeException ex) {
            // Christian: faengt auch eine falsch geschriebene Zahl ab.
            ex.printStackTrace();
        }

        return liste;
    }

    // Christian: Alle Benutzernamen aus der Datei, ohne Doppelte.
    public static List<String> alleBenutzer() {
        Set<String> namen = new LinkedHashSet<>();
        for (StepEntry e : readAll()) {
            namen.add(e.username);
        }
        return new ArrayList<>(namen);
    }

    // Christian: Beispieldaten - 3 Benutzer, je 14 Tage. Nur beim ersten Start.
    private static void seedDemo() {
        String[] benutzer = {"Rene", "Hasan", "John"};
        int[] ziele = {10000, 12000, 8000};

        for (int b = 0; b < benutzer.length; b++) {
            for (int i = 0; i < 14; i++) {
                LocalDate tag = LocalDate.now().minusDays(i);
                int schritte = 5000 + (i % 6) * 1200 + b * 500;
                append(new StepEntry(benutzer[b], tag, schritte, ziele[b]));
            }
        }
    }
}
