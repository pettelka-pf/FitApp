package com.fitapp.model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Christian: Rechnet die Schritt-Zahlen und aufgenommenen Kalorien
 * fuer einen Benutzer und einen Zeitraum aus.
 *
 * Die Daten kommen aus der Datenbank:
 * - Schritte aus der Tabelle "steps"
 * - aufgenommene Kalorien aus der Tabelle "meals"
 *
 * Frueher war das eine CSV-Datei.
 */
public class Statistic {

    // Christian: alter Teil - bleibt, weil StatisticTest ihn braucht.
    private String period;
    private int numOfExercises;

    public Statistic(String period, int numOfExercises) {
        this.period = period;
        this.numOfExercises = numOfExercises;
    }

    public String getPeriod() {
        return period;
    }

    public int getNumOfExercises() {
        return numOfExercises;
    }

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

    // Christian: neuer Teil - Auswertung aus der Datenbank.

    private LocalDate von;
    private LocalDate bis;

    // Christian: Datum -> Schritte an dem Tag.
    // Einmal geladen, dann nur noch nachschlagen.
    private Map<LocalDate, Integer> schritteProTag = new HashMap<>();

    // Christian: Datum -> aufgenommene Kalorien an dem Tag.
    private Map<LocalDate, Integer> kalorienProTag = new HashMap<>();

    // Christian: Tagesziel des Benutzers (users.daily_step_goal).
    private int tagesziel = 0;

    // Christian: Konstruktor fuer die Statistik-Seite - Benutzer-Id und Zeitraum.
    public Statistic(int userId, LocalDate von, LocalDate bis) {
        this.von = von;
        this.bis = bis;
        ladeAusDatenbank(userId);
    }

    // Christian: Schritte, Kalorien und Tagesziel aus der DB holen.
    private void ladeAusDatenbank(int userId) {

        try {
            Connection conn =
                    DatabaseManager.getInstance().getConnection();

            // -------------------------------------------------
            // SCHRITTE
            // -------------------------------------------------

            // Christian: Schritte pro Tag im Zeitraum.
            // SUM, weil ein Tag mehrere Eintraege haben kann.
            String sqlSteps =
                    "SELECT date, SUM(count) AS summe FROM steps "
                            + "WHERE user_id = ? AND date BETWEEN ? AND ? "
                            + "GROUP BY date";

            try (PreparedStatement ps =
                         conn.prepareStatement(sqlSteps)) {

                ps.setInt(1, userId);
                ps.setDate(2, Date.valueOf(von));
                ps.setDate(3, Date.valueOf(bis));

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        schritteProTag.put(
                                rs.getDate("date").toLocalDate(),
                                rs.getInt("summe")
                        );
                    }
                }
            }

            // -------------------------------------------------
            // AUFGENOMMENE KALORIEN
            // -------------------------------------------------

            // Christian: Aufgenommene Kalorien pro Tag.
            // SUM, weil ein Benutzer mehrere Mahlzeiten pro Tag
            // eingetragen haben kann.
            String sqlCalories =
                    "SELECT date, SUM(calories) AS summe FROM meals "
                            + "WHERE user_id = ? AND date BETWEEN ? AND ? "
                            + "GROUP BY date";

            try (PreparedStatement ps =
                         conn.prepareStatement(sqlCalories)) {

                ps.setInt(1, userId);
                ps.setDate(2, Date.valueOf(von));
                ps.setDate(3, Date.valueOf(bis));

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        kalorienProTag.put(
                                rs.getDate("date").toLocalDate(),
                                rs.getInt("summe")
                        );
                    }
                }
            }

            // -------------------------------------------------
            // TAGESZIEL SCHRITTE
            // -------------------------------------------------

            // Christian: Tagesziel des Benutzers.
            try (PreparedStatement ps =
                         conn.prepareStatement(
                                 "SELECT daily_step_goal FROM users WHERE id = ?")) {

                ps.setInt(1, userId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        tagesziel = rs.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {

            // Christian: bei einem DB-Fehler bleibt die Statistik
            // einfach leer.
            e.printStackTrace();
        }
    }

    // Christian: Anzahl Tage im Zeitraum
    // (Start- und Endtag zaehlen mit).
    private long tageImZeitraum() {
        return ChronoUnit.DAYS.between(von, bis) + 1;
    }

    // -------------------------------------------------
    // SCHRITTE
    // -------------------------------------------------

    // Christian: Schritte an einem Tag.
    public int schritteAmTag(LocalDate tag) {
        return schritteProTag.getOrDefault(tag, 0);
    }

    // Christian: Summe aller Schritte im Zeitraum.
    public int schritteImZeitraum() {

        int summe = 0;

        for (int wert : schritteProTag.values()) {
            summe += wert;
        }

        return summe;
    }

    // Christian: geplante Schritte = Tagesziel * Anzahl Tage.
    public int geplanteSchritte() {
        return tagesziel * (int) tageImZeitraum();
    }

    // Christian: Ist minus Soll.
    // Negativ = weniger als geplant.
    public int schritteDifferenz() {
        return schritteImZeitraum() - geplanteSchritte();
    }

    // Christian: grobe Kalorien - ca. 0,04 kcal pro Schritt.
    public double verbrannteKalorien() {
        return schritteImZeitraum() * 0.04;
    }

    // Christian: Durchschnitt Schritte pro Tag.
    public double durchschnittProTag() {

        long tage = tageImZeitraum();

        if (tage <= 0) {
            return 0;
        }

        return schritteImZeitraum() / (double) tage;
    }

    // Christian: an wie vielen Tagen das Tagesziel erreicht wurde.
    public int tageZielErreicht() {

        if (tagesziel <= 0) {
            return 0;
        }

        int erreicht = 0;

        for (LocalDate tag = von;
             !tag.isAfter(bis);
             tag = tag.plusDays(1)) {

            if (schritteAmTag(tag) >= tagesziel) {
                erreicht++;
            }
        }

        return erreicht;
    }

    // Christian: Tagesziel des Benutzers.
    public int getTagesziel() {
        return tagesziel;
    }

    // -------------------------------------------------
    // AUFGENOMMENE KALORIEN
    // -------------------------------------------------

    /**
     * Christian: aufgenommene Kalorien an einem bestimmten Tag.
     *
     * Wenn an diesem Tag keine Mahlzeit eingetragen wurde,
     * wird 0 zurueckgegeben.
     */
    public int kalorienAmTag(LocalDate tag) {
        return kalorienProTag.getOrDefault(tag, 0);
    }

    /**
     * Christian: Summe aller aufgenommenen Kalorien im Zeitraum.
     */
    public int kalorienImZeitraum() {

        int summe = 0;

        for (int wert : kalorienProTag.values()) {
            summe += wert;
        }

        return summe;
    }

    /**
     * Christian: Durchschnittlich aufgenommene Kalorien pro Tag.
     */
    public double durchschnittKalorienProTag() {

        long tage = tageImZeitraum();

        if (tage <= 0) {
            return 0;
        }

        return kalorienImZeitraum() / (double) tage;
    }
}
