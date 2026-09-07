package com.fitapp.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SQLite-backed implementation of {@link UserRepository}.
 * Replaces the CSV-based approach with a persistent relational database.
 */
public class UserDatabaseSQLite implements UserRepository {

    @Override
    public void validateInput(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new EmptyFieldException("Username");
        }
        if (password == null || password.isBlank()) {
            throw new EmptyFieldException("Password");
        }
        if (!validateUser(username, password)) {
            throw new InvalidCredentialsException();
        }
    }

    @Override
    public boolean validateUser(String username, String password) {
        String sql = "SELECT 1 FROM users WHERE username = ? AND password = ?";
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, PasswordHasher.hash(password));
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // New User:
    public boolean addUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (? , ?)";
        try (PreparedStatement stmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, PasswordHasher.hash(password));
            stmt.executeUpdate();
            System.out.println("User added: " + username);
            return true;

        } catch (SQLException e) {
            System.out.println("User konnte nicht angelegt werden: " + e.getMessage());
            return false;
        }
    }

    public void updatePassword(String username, String password) {
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        try (PreparedStatement stmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setString(1, PasswordHasher.hash(password));
            stmt.setString(2, username);
            stmt.executeUpdate();
            System.out.println("Password updated for: " + username);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

   // @Override
    public void registerUser(String username, String password) {
        addUser(username, password);
    }

    /** @return die id des Benutzers, oder Session.NO_USER wenn es ihn nicht gibt */
    public int findIdByUsername(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement stmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : Session.NO_USER;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Session.NO_USER;
        }
    }
}