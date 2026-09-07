package com.fitapp.model;

import java.sql.SQLException;
import java.time.LocalDate;

/** Repository Pattern, aufgebaut wie CalorieRepository. */
public interface StepRepository {

    /** Tagesziel aus users.daily_step_goal. */
    int getGoal(int userId) throws SQLException;

    /** Summe aller Schritt-Einträge des Tages. */
    int getStepsToday(int userId, LocalDate date) throws SQLException;

    void setGoal(int userId, int goal) throws SQLException;

    void addSteps(int userId, int count, LocalDate date) throws SQLException;

    /** Löscht alle Einträge eines Tages. Das Ziel bleibt bestehen. */
    void resetSteps(int userId, LocalDate date) throws SQLException;
}