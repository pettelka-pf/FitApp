package com.fitapp.model;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Repository Pattern: der Controller kennt nur dieses Interface,
 * nicht die konkrete Datenbank dahinter. Aufgebaut wie UserRepository.
 */
public interface CalorieRepository {

    /** Tagesziel aus users.daily_calorie_goal. */
    int getGoal(int userId) throws SQLException;

    /** Summe aller Mahlzeiten des Tages. */
    int getEatenToday(int userId, LocalDate date) throws SQLException;

    /** Summe der verbrannten Kalorien des Tages aus exercises. */
    int getBurnedToday(int userId, LocalDate date) throws SQLException;

    /** Setzt das Tagesziel des Benutzers. */
    void setGoal(int userId, int goal) throws SQLException;

    /** Traegt eine Mahlzeit ein. */
    void addMeal(int userId, String name, int calories, LocalDate date) throws SQLException;

    /** Loescht alle Mahlzeiten eines Tages. Das Ziel bleibt bestehen. */
    void resetMeals(int userId, LocalDate date) throws SQLException;
}