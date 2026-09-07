package com.fitapp.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Christian: Rechnet die Schritt-Zahlen fuer einen Benutzer und einen Zeitraum aus.
 * Die Daten kommen aus steps.csv (Klasse StepCsv).
 * Jede Methode geht die Liste einmal durch und zaehlt zusammen.
 */
public class Statistic {

    // Christian: alter Teil - bleibt, weil StatisticTest ihn braucht.
    private String period;
    private int numOfExercises;

    public Statistic(String period, int numOfExercises) {
        this.period = period;
        this.numOfExercises = numOfExercises;
    }

    public String getPeriod() { return period; }
    public int getNumOfExercises() { return numOfExercises; }

    public void calcStatistic() {
        // to be implemented
    }

    public double getAverage() {
        // to be implemented
        return 0;
    }

    public String export() {
        // to be implemented
        return "Implement this method.";
    }

    // Christian: neuer Teil - Auswertung der Schritte aus steps.csv.

    private String username;
    private LocalDate von;
    private LocalDate bis;

    // Christian: Datei einmal lesen und merken. Spart wiederholten Plattenzugriff.
    private List<StepEntry> alleEintraege;

    // Christian: Konstruktor fuer die Statistik-Seite - Benutzer und Zeitraum.
    public Statistic(String username, LocalDate von, LocalDate bis) {
        this.username = username;
        this.von = von;
        this.bis = bis;
        this.alleEintraege = StepCsv.readAll();
    }

    // Christian: true, wenn der Eintrag zum Benutzer gehoert und im Zeitraum liegt.
    private boolean passt(StepEntry e) {
        return e.username.equals(username)
                && !e.datum.isBefore(von)
                && !e.datum.isAfter(bis);
    }

    // Christian: Anzahl Tage im Zeitraum (Start- und Endtag zaehlen mit).
    private long tageImZeitraum() {
        return ChronoUnit.DAYS.between(von, bis) + 1;
    }

    // Christian: Schritte an einem Tag. Nutzt das Balkendiagramm.
    public int schritteAmTag(LocalDate tag) {
        int summe = 0;
        for (StepEntry e : alleEintraege) {
            if (e.username.equals(username) && e.datum.equals(tag)) {
                summe += e.schritte;
            }
        }
        return summe;
    }

    // Christian: Summe aller Schritte im Zeitraum.
    public int schritteImZeitraum() {
        int summe = 0;
        for (StepEntry e : alleEintraege) {
            if (passt(e)) {
                summe += e.schritte;
            }
        }
        return summe;
    }

    // Christian: geplante Schritte = Tagesziel * Anzahl Tage.
    public int geplanteSchritte() {
        return letztesZiel() * (int) tageImZeitraum();
    }

    // Christian: Ist minus Soll. Negativ = weniger als geplant.
    public int schritteDifferenz() {
        return schritteImZeitraum() - geplanteSchritte();
    }

    // Christian: grobe Kalorien - ca. 0,04 kcal pro Schritt.
    public double verbrannteKalorien() {
        return schritteImZeitraum() * 0.04;
    }

    // Christian: Durchschnitt Schritte pro Tag.
    public double schritteProTag() {
        long tage = tageImZeitraum();
        if (tage <= 0) {
            return 0;
        }
        return schritteImZeitraum() / (double) tage;
    }

    // Christian: an wie vielen Tagen das Tagesziel erreicht wurde.
    public int tageZielErreicht() {
        int erreicht = 0;

        // Christian: jeden Tag im Zeitraum durchgehen.
        for (LocalDate tag = von; !tag.isAfter(bis); tag = tag.plusDays(1)) {

            int summeAmTag = 0;
            int zielAmTag = 0;

            for (StepEntry e : alleEintraege) {
                if (e.username.equals(username) && e.datum.equals(tag)) {
                    summeAmTag += e.schritte;
                    zielAmTag = e.ziel;
                }
            }

            if (zielAmTag > 0 && summeAmTag >= zielAmTag) {
                erreicht++;
            }
        }

        return erreicht;
    }

    // Christian: das zuletzt gespeicherte Tagesziel des Benutzers.
    private int letztesZiel() {
        int ziel = 0;
        for (StepEntry e : alleEintraege) {
            if (e.username.equals(username)) {
                ziel = e.ziel;
            }
        }
        return ziel;
    }
}
