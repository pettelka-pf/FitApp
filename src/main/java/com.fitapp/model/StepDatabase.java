package com.fitapp.model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/** Jede Query fasst genau eine Tabelle an. */
public class StepDatabase implements StepRepository {

    @Override
    public int getGoal(int userId) throws SQLException {
        return queryInt("SELECT daily_step_goal FROM users WHERE id = ?", userId, null);
    }

    @Override
    public int getStepsToday(int userId, LocalDate date) throws SQLException {
        return queryInt("SELECT SUM(count) FROM steps WHERE user_id = ? AND date = ?",
                userId, date);
    }

    @Override
    public void setGoal(int userId, int goal) throws SQLException {
        String sql = "UPDATE users SET daily_step_goal = ? WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, goal);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    @Override
    public void addSteps(int userId, int count, LocalDate date) throws SQLException {
        String sql = "INSERT INTO steps (user_id, count, date) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, count);
            ps.setDate(3, Date.valueOf(date));
            ps.executeUpdate();
        }
    }

    @Override
    public void resetSteps(int userId, LocalDate date) throws SQLException {
        String sql = "DELETE FROM steps WHERE user_id = ? AND date = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(date));
            ps.executeUpdate();
        }
    }

    private Connection connection() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    /** @param date null, wenn die Query keinen Datums,Platzhalter hat */
    private int queryInt(String sql, int userId, LocalDate date) throws SQLException {
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (date != null) {
                ps.setDate(2, Date.valueOf(date));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}