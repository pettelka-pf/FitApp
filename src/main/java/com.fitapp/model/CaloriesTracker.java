package com.fitapp.model;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class CaloriesTracker {

    // -------------------------
    // ATTRIBUTES
    // -------------------------

    /**
     * Anzahl der gegessenen Kalorien.
     */
    private final IntegerProperty consumed =
            new SimpleIntegerProperty(0);

    /**
     * Tägliches Kalorienziel.
     */
    private final IntegerProperty dailyLimit;

    /**
     * Durch Sport verbrannte Kalorien.
     *
     * Diese erhöhen das verfügbare Tagesbudget.
     */
    private final IntegerProperty burned =
            new SimpleIntegerProperty(0);


    // -------------------------
    // CONSTRUCTOR
    // -------------------------

    public CaloriesTracker(int dailyLimit) {
        this.dailyLimit =
                new SimpleIntegerProperty(dailyLimit);
    }


    // -------------------------
    // GETTERS
    // -------------------------

    public IntegerProperty getDailyLimit() {
        return dailyLimit;
    }

    public IntegerProperty getConsumed() {
        return consumed;
    }

    public IntegerProperty getBurned() {
        return burned;
    }


    // -------------------------
    // BURNED CALORIES
    // -------------------------

    /**
     * Setzt die heute verbrannten Kalorien.
     *
     * @param calories darf nicht negativ sein
     */
    public void setBurned(int calories) {

        if (calories < 0) {
            throw new NegativeCaloriesException();
        }

        burned.set(calories);
    }


    // -------------------------
    // ADD CALORIES
    // -------------------------

    /**
     * Fügt gegessene Kalorien hinzu.
     *
     * Eine Überschreitung des Tagesziels ist erlaubt.
     * Dadurch kann der Controller erkennen, wann das
     * Kalorienziel überschritten wurde.
     */
    public void addCalories(int calories) {

        if (calories < 0) {
            throw new NegativeCaloriesException();
        }

        consumed.set(
                consumed.get() + calories
        );
    }


    // -------------------------
    // RESET
    // -------------------------

    /**
     * Setzt die gegessenen Kalorien auf 0 zurück.
     */
    public void reset() {
        consumed.set(0);
    }


    // -------------------------
    // REMAINING CALORIES
    // -------------------------

    /**
     * Berechnet die verbleibenden Kalorien.
     *
     * Formel:
     *
     * dailyLimit - consumed + burned
     *
     * Der Wert wird auf mindestens 0 begrenzt.
     * Dadurch wird bei einer Überschreitung nicht
     * ins Minus gezählt.
     *
     * Die tatsächlich gegessenen Kalorien bleiben
     * weiterhin in consumed gespeichert.
     */
    public IntegerBinding remainingCaloriesProperty() {

        return Bindings.createIntegerBinding(
                () -> Math.max(
                        0,
                        dailyLimit.get()
                                - consumed.get()
                                + burned.get()
                ),

                dailyLimit,
                consumed,
                burned
        );
    }

}
