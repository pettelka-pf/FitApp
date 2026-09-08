package com.fitapp.model;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface ExerciseRepository {

    int save(
            int userId,
            Exercise exercise
    ) throws SQLException;


    List<Exercise> findByUser(
            int userId
    ) throws SQLException;


    List<Exercise> findByUserAndDate(
            int userId,
            LocalDate date
    ) throws SQLException;


    Exercise findById(
            int userId,
            int exerciseId
    ) throws SQLException;
}
