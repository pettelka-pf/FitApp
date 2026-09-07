package com.fitapp.model;

import java.sql.SQLException;
import java.util.List;

public class ExerciseService {

    private final ExerciseRepository repository = new ExerciseDatabase();

    /** Speichert die Uebung und liefert sie mit der von der DB vergebenen id zurueck. */
    public Exercise addExercise(int userId, Exercise exercise) throws SQLException {
        if (exercise == null) {
            return null;
        }
        int id = repository.save(userId, exercise);
        return repository.findByUser(userId).stream()
                .filter(e -> e.getId() == id)
                .findFirst()
                .orElse(exercise);
    }

    public List<Exercise> getAllExercises(int userId) throws SQLException {
        return repository.findByUser(userId);
    }

    public Exercise getExerciseById(int userId, int id) throws SQLException {
        return getAllExercises(userId).stream()
                .filter(exercise -> exercise.getId() == id)
                .findFirst()
                .orElse(null);
    }
}