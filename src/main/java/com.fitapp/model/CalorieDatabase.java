package com.fitapp.model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * PostgreSQL-Implementierung von {@link CalorieRepository}.

 * Jede Query fasst genau eine Tabelle an. Ein JOIN ueber meals und exercises
 * wuerde beide Summen vervielfachen, sobald es mehrere Zeilen pro Tag gibt.
 */
public class CalorieDatabase implements CalorieRepository {

    @Override
    public int getGoal(int userId) throws SQLException {
        return queryInt("SELECT daily_calorie_goal FROM users WHERE id = ?", userId, null);
    }

    @Override
    public int getEatenToday(int userId, LocalDate date) throws SQLException {
        return queryInt("SELECT SUM(calories) FROM meals WHERE user_id = ? AND date = ?",
                userId, date);
    }

    @Override
    public int getBurnedToday(int userId, LocalDate date) throws SQLException {
        return queryInt("SELECT SUM(calories_burned) FROM exercises WHERE user_id = ? AND date = ?",
                userId, date);
    }

    @Override
    public void setGoal(int userId, int goal) throws SQLException {
        String sql = "UPDATE users SET daily_calorie_goal = ? WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, goal);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    @Override
    public void addMeal(int userId, String name, int calories, LocalDate date) throws SQLException {
        String sql = "INSERT INTO meals (user_id, name, calories, date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setInt(3, calories);
            ps.setDate(4, Date.valueOf(date));
            ps.executeUpdate();
        }
    }

    @Override
    public void resetMeals(int userId, LocalDate date) throws SQLException {
        String sql = "DELETE FROM meals WHERE user_id = ? AND date = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(date));
            ps.executeUpdate();
        }
    }

    private Connection connection() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    /**
     * Liest eine einzelne Zahl.
     *
     * SUM() liefert NULL, wenn es für den Tag keine Zeile gibt. getInt() macht
     * daraus 0, deshalb braucht es kein COALESCE. exercises.calories ist
     * DOUBLE PRECISION, darum wird gerundet statt abgeschnitten.
     *
     * @param date null, wenn die Query keinen Datums-Platzhalter hat
     */
    private int queryInt(String sql, int userId, LocalDate date) throws SQLException {
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (date != null) {
                ps.setDate(2, Date.valueOf(date));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? (int) Math.round(rs.getDouble(1)) : 0;
            }
        }
    }
}