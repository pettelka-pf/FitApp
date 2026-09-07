package com.fitapp.model;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;

/**
 * Singleton that manages the SQLite connection and schema initialization.
 * The database file is stored in ~/.fitapp/fitapp.db.
 */
public class DatabaseManager {

    //private static final String DB_DIR = System.getProperty("user.home") + "/.fitapp";
    private static final Dotenv dotenv = Dotenv.load();
    private static final String URL =  dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() throws SQLException {
        //connection = DriverManager.getConnection(URL);
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
        initializeDatabase();
    }

    /**
     * Returns the singleton instance, creating it on first access.
     *
     * @return the DatabaseManager instance
     * @throws SQLException if the connection cannot be established
     */
    public static synchronized DatabaseManager getInstance() throws SQLException {
        if (instance == null || !instance.isConnectionUsable()) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Returns the active SQLite connection.
     *
     * @return the Connection
     */
    public Connection getConnection() {
        return connection;
    }
    public static synchronized void shutdown() {
        if (instance != null) {
            try {
                if (!instance.connection.isClosed()) {
                    instance.connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Closing DB connection failed: " + e.getMessage());
            }
            instance = null;
        }
    }

    private boolean isConnectionUsable(){
        try {
            return connection != null && !connection.isClosed() &&connection.isValid(3);
        }catch (SQLException e){
            return false;
        }
    }

    private void initializeDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Users Tabelle (schon da)
            stmt.execute(
                    """
                      CREATE TABLE IF NOT EXISTS users (
                      id SERIAL PRIMARY KEY,   
                    username TEXT NOT NULL UNIQUE, 
                    password TEXT NOT NULL,  
                     daily_calorie_goal Integer default 2000
                     )"""
            );

            // Exercises Tabelle
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS exercises ("
                            + "id SERIAL PRIMARY KEY, "
                            + "user_id INTEGER REFERENCES users(id), "
                            + "type TEXT NOT NULL, "
                            + "name TEXT NOT NULL, "
                            + "description TEXT, "
                            + "difficulty TEXT, "
                            + "duration DOUBLE PRECISION, "
                            + "calories DOUBLE PRECISION, "
                            + "date DATE, "
                            // WeightExercise Felder
                            + "weight DOUBLE PRECISION, "
                            + "repetition INTEGER, "
                            + "muscle_group TEXT, "
                            // CardioRunning Felder
                            + "distance DOUBLE PRECISION, "
                            + "speed DOUBLE PRECISION, "
                            + "steps INTEGER, "
                            // CardioCalisthenics Felder
                            + "interval_time DOUBLE PRECISION, "
                            + "exercises_per_round INTEGER, "
                            + "rounds INTEGER"
                            + ")"
            );

            // Plans Tabelle
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS plans ("
                            + "id SERIAL PRIMARY KEY, "
                            + "user_id INTEGER REFERENCES users(id), "
                            + "name TEXT NOT NULL, "
                            + "start_date DATE, "
                            + "end_date DATE"
                            + ")"
            );

            // Plan-Exercises Verbindung
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS plan_exercises ("
                            + "plan_id INTEGER REFERENCES plans(id), "
                            + "exercise_id INTEGER REFERENCES exercises(id), "
                            + "PRIMARY KEY (plan_id, exercise_id)"
                            + ")"
            );

            // Meals Tabelle (für Caloric Intake)
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS meals ("
                            + "id SERIAL PRIMARY KEY, "
                            + "user_id INTEGER REFERENCES users(id), "
                            + "name TEXT NOT NULL, "
                            + "calories INTEGER NOT NULL, "
                            + "date DATE"
                            + ")"
            );
            stmt.execute(
                    """
                            Create table if not exists steps(
                            id serial primary key,
                            user_id integer References users(id),
                            count integer not NULL,
                            date Date                       
                            )
                            """
            );
            stmt.execute("Alter table users add column if not exists daily_calorie_goal integer Default 2000" );
            stmt.execute("UPDATE users set daily_calorie_goal = 2000 where daily_calorie_goal is null" );
            stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS daily_step_goal INTEGER DEFAULT 10000");
            stmt.execute("UPDATE users SET daily_step_goal = 10000 WHERE daily_step_goal IS NULL");
            stmt.execute("ALTER TABLE exercises ADD COLUMN IF NOT EXISTS calories_burned DOUBLE PRECISION DEFAULT 0");
            seedDefaultUsers(stmt);
        }
    }

    private void seedDefaultUsers(Statement stmt) throws SQLException {
        String hashed = PasswordHasher.hash("1234");
        String sql = "Insert into users(username, password) values (?, ?) on conflict do nothing";
//        stmt.execute("INSERT INTO users (username, password) VALUES ('Hasan', '1234') ON CONFLICT DO NOTHING");
//        stmt.execute("INSERT INTO users (username, password) VALUES ('John', '1234') ON CONFLICT DO NOTHING");
//        stmt.execute("INSERT INTO users (username, password) VALUES ('Rene', '1234') ON CONFLICT DO NOTHING");
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            for(String username : new String[]{"Hasan", "John", "Rene"}){
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, hashed);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
}