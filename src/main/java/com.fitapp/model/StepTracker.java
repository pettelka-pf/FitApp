package com.fitapp.model;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class StepTracker {

    private final IntegerProperty dailyGoal;
    private final IntegerProperty currentSteps = new SimpleIntegerProperty(0);
    private final IntegerBinding remainingSteps;

    public StepTracker(int dailyGoal) {
        this.dailyGoal = new SimpleIntegerProperty(dailyGoal);
        this.remainingSteps =
                Bindings.createIntegerBinding(
                        () -> Math.max(
                                0,
                                this.dailyGoal.get() - currentSteps.get()
                        ),
                        this.dailyGoal,
                        currentSteps
                );

    }

    public void addSteps(int steps) {

        if (steps < 0) {
            throw new NegativeStepsException();
        }
        currentSteps.set(currentSteps.get() + steps);
    }

    /**
     * Setzt den Stand aus der Datenbank. Bewusst ohne Limit-Prüfung:
     * ein später gesenktes Ziel darf das Laden nicht blockieren.
     */
    public void setSteps(int steps) {
        if (steps < 0) {
            throw new NegativeStepsException();
        }
        currentSteps.set(steps);
    }

    public void reset() {
        currentSteps.set(0);
    }

    public IntegerBinding remainingStepsProperty() {
        return remainingSteps;
    }

    public IntegerProperty getDailyGoal() {
        return dailyGoal;
    }

    public IntegerProperty currentStepsProperty() {
        return currentSteps;
    }
}