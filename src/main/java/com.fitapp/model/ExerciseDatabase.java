package com.fitapp.model;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ExerciseDatabase implements ExerciseRepository {

    /**
     * Typen der Unterklassen-Spalten, in Reihenfolge der Platzhalter 10..18.
     * Wird gebraucht,weil PostgreSQL bei setNull den Zieltyp kennen will.
     */
    private static final int[] SUB_TYPES = {
            Types.DOUBLE, Types.INTEGER, Types.VARCHAR,   // weight, repetition, muscle_group
            Types.DOUBLE, Types.DOUBLE, Types.INTEGER,    // distance, speed, steps
            Types.DOUBLE, Types.INTEGER, Types.INTEGER    // interval_time, exercises_per_round, rounds
    };

    @Override
    public int save(int userId, Exercise exercise) throws SQLException {
        String sql = """
                INSERT INTO exercises
                  (user_id, type, name, description, difficulty, duration, calories, calories_burned, date,
                   weight, repetition, muscle_group,
                   distance, speed, steps,
                   interval_time, exercises_per_round, rounds)
                VALUES (?,?,?,?,?,?,?,?,?, ?,?,?, ?,?,?, ?,?,?)
                RETURNING id
                """;

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, typeOf(exercise));
            ps.setString(3, exercise.getName());
            ps.setString(4, exercise.getDescription());
            ps.setString(5, exercise.getDifficulty());
            ps.setDouble(6, exercise.getDuration());
            ps.setDouble(7, exercise.getCalories());          // kcal pro Stunde
            ps.setDouble(8, exercise.calcCalories());         // tatsächlich verbrannt
            ps.setDate(9, Date.valueOf(toLocalDate(exercise.getDate())));

            // erst alles auf NULL, danach nur die Spalten der passenden Unterklasse
            for (int i = 0; i < SUB_TYPES.length; i++) {
                ps.setNull(10 + i, SUB_TYPES[i]);
            }

            if (exercise instanceof WeightExercise w) {
                ps.setDouble(10, w.getWeight());
                ps.setInt(11, w.getRepetition());
                ps.setString(12, w.getMuscleGroup());

            } else if (exercise instanceof CardioRunningExercise r) {
                ps.setDouble(13, r.getDistance());
                ps.setDouble(14, r.getSpeed());
                ps.setInt(15, r.getSteps());

            } else if (exercise instanceof CardioCalisthenicsExercise c) {
                ps.setDouble(16, c.getInterval());
                ps.setInt(17, c.getNumOfExercisesPerRound());
                ps.setInt(18, c.getRound());
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    @Override
    public List<Exercise> findByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM exercises WHERE user_id = ? ORDER BY id";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return readAll(ps);
        }
    }

    @Override
    public List<Exercise> findByUserAndDate(int userId, LocalDate date) throws SQLException {
        String sql = "SELECT * FROM exercises WHERE user_id = ? AND date = ? ORDER BY id";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(date));
            return readAll(ps);
        }
    }

    private List<Exercise> readAll(PreparedStatement ps) throws SQLException {
        List<Exercise> result = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(fromRow(rs));
            }
        }
        return result;
    }

    /**
     * Discriminator- Spalte type entscheidet über die Unterklasse.
     * Genau diese Verzweigung wandert später in die ExerciseFactory,
     * dann ruft diese Methode nur noch die Factory auf.
     */
    private Exercise fromRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String type = rs.getString("type");
        String description = rs.getString("description");
        java.util.Date date = rs.getDate("date");
        String difficulty = rs.getString("difficulty");
        double duration = rs.getDouble("duration");
        double calories = rs.getDouble("calories");

        return switch (type == null ? "" : type.toUpperCase()) {
            case "WEIGHT" -> new WeightExercise(
                    id, name, description, date, difficulty, duration, calories,
                    rs.getDouble("weight"),
                    rs.getInt("repetition"),
                    rs.getString("muscle_group"));

            case "CARDIO_RUNNING" -> new CardioRunningExercise(
                    id, name, description, date, difficulty, duration, calories,
                    rs.getDouble("distance"),
                    rs.getDouble("speed"),
                    rs.getInt("steps"));

            case "CARDIO_CALISTHENICS" -> new CardioCalisthenicsExercise(
                    id, name, description, date, difficulty, duration, calories,
                    rs.getDouble("interval_time"),
                    rs.getInt("exercises_per_round"),
                    rs.getInt("rounds"));
            default -> throw new SQLException("Unknown exercise type: " + type);
        };
    }

    private String typeOf(Exercise exercise) {
        if (exercise instanceof WeightExercise) {
            return "WEIGHT";
        }
        if (exercise instanceof CardioRunningExercise) {
            return "CARDIO_RUNNING";
        }
        return "CARDIO_CALISTHENICS";
    }

    /** java.sql.Date kann kein toInstant(), deshalb die Fallunterscheidung. */
    private LocalDate toLocalDate(java.util.Date date) {
        if (date == null) {
            return LocalDate.now();
        }
        if (date instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Connection connection() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }
}