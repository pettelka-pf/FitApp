package com.fitapp.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanDatabase implements PlanRepository {

    private final ExerciseRepository exerciseRepository =
            new ExerciseDatabase();


    // =====================================================
    // SAVE
    // =====================================================

    @Override
    public int save(
            int userId,
            Plan plan) throws SQLException {

        if (plan == null) {
            throw new IllegalArgumentException(
                    "Plan cannot be null."
            );
        }


        Connection connection =
                DatabaseManager
                        .getInstance()
                        .getConnection();


        boolean oldAutoCommit =
                connection.getAutoCommit();


        try {

            connection.setAutoCommit(false);


            int planId;


            // =================================================
            // PLAN INSERT / UPDATE
            // =================================================

            if (plan.getId() <= 0) {

                String insertSql =
                        """
                        INSERT INTO plans
                            (user_id, name, start_date, end_date)
                        VALUES
                            (?, ?, ?, ?)
                        RETURNING id
                        """;


                try (PreparedStatement ps =
                             connection.prepareStatement(
                                     insertSql
                             )) {

                    ps.setInt(
                            1,
                            userId
                    );

                    ps.setString(
                            2,
                            plan.getName()
                    );

                    ps.setDate(
                            3,
                            toSqlDate(
                                    plan.getStartDate()
                            )
                    );

                    ps.setDate(
                            4,
                            toSqlDate(
                                    plan.getEndDate()
                            )
                    );


                    try (ResultSet rs =
                                 ps.executeQuery()) {

                        if (!rs.next()) {

                            throw new SQLException(
                                    "Could not create plan."
                            );
                        }


                        planId =
                                rs.getInt(1);
                    }
                }


                // ID im Java-Objekt setzen
                plan.setId(planId);

            } else {

                planId =
                        plan.getId();


                String updateSql =
                        """
                        UPDATE plans
                        SET name = ?,
                            start_date = ?,
                            end_date = ?
                        WHERE id = ?
                          AND user_id = ?
                        """;


                try (PreparedStatement ps =
                             connection.prepareStatement(
                                     updateSql
                             )) {

                    ps.setString(
                            1,
                            plan.getName()
                    );

                    ps.setDate(
                            2,
                            toSqlDate(
                                    plan.getStartDate()
                            )
                    );

                    ps.setDate(
                            3,
                            toSqlDate(
                                    plan.getEndDate()
                            )
                    );

                    ps.setInt(
                            4,
                            planId
                    );

                    ps.setInt(
                            5,
                            userId
                    );


                    ps.executeUpdate();
                }
            }


            // =================================================
            // ALTE PLAN-EXERCISES LÖSCHEN
            // =================================================

            String deleteExercisesSql =
                    """
                    DELETE FROM plan_exercises
                    WHERE plan_id = ?
                    """;


            try (PreparedStatement ps =
                         connection.prepareStatement(
                                 deleteExercisesSql
                         )) {

                ps.setInt(
                        1,
                        planId
                );

                ps.executeUpdate();
            }


            // =================================================
            // AKTUELLE PLAN-EXERCISES SPEICHERN
            // =================================================

            String insertExerciseSql =
                    """
                    INSERT INTO plan_exercises
                        (
                            plan_id,
                            day_name,
                            exercise_id,
                            duration,
                            sets,
                            reps
                        )
                    VALUES
                        (?, ?, ?, ?, ?, ?)
                    """;


            try (PreparedStatement ps =
                         connection.prepareStatement(
                                 insertExerciseSql
                         )) {


                for (PlanDay day :
                        plan.getDays()) {


                    for (PlanExercise planExercise :
                            day.getExercises()) {


                        ps.setInt(
                                1,
                                planId
                        );


                        ps.setString(
                                2,
                                day.getDayName()
                        );


                        ps.setInt(
                                3,
                                planExercise
                                        .getExercise()
                                        .getId()
                        );


                        ps.setDouble(
                                4,
                                planExercise
                                        .getDuration()
                        );


                        ps.setInt(
                                5,
                                planExercise
                                        .getSets()
                        );


                        ps.setInt(
                                6,
                                planExercise
                                        .getReps()
                        );


                        ps.addBatch();
                    }
                }


                ps.executeBatch();
            }


            connection.commit();


            return planId;

        } catch (SQLException e) {

            connection.rollback();

            throw e;

        } finally {

            connection.setAutoCommit(
                    oldAutoCommit
            );
        }
    }


    // =====================================================
    // FIND BY USER
    // =====================================================

    @Override
    public List<Plan> findByUser(
            int userId) throws SQLException {


        Connection connection =
                DatabaseManager
                        .getInstance()
                        .getConnection();


        List<Plan> plans =
                new ArrayList<>();


        String planSql =
                """
                SELECT
                    id,
                    name,
                    start_date,
                    end_date
                FROM plans
                WHERE user_id = ?
                ORDER BY id
                """;


        // -------------------------------------------------
        // Exercises des Users einmal laden
        // -------------------------------------------------

        List<Exercise> userExercises =
                exerciseRepository.findByUser(
                        userId
                );


        Map<Integer, Exercise> exerciseMap =
                new HashMap<>();


        for (Exercise exercise :
                userExercises) {

            exerciseMap.put(
                    exercise.getId(),
                    exercise
            );
        }


        // -------------------------------------------------
        // Plans laden
        // -------------------------------------------------

        try (PreparedStatement ps =
                     connection.prepareStatement(
                             planSql
                     )) {

            ps.setInt(
                    1,
                    userId
            );


            try (ResultSet rs =
                         ps.executeQuery()) {


                while (rs.next()) {

                    int planId =
                            rs.getInt("id");


                    String name =
                            rs.getString("name");


                    Date startDate =
                            rs.getDate(
                                    "start_date"
                            );


                    Date endDate =
                            rs.getDate(
                                    "end_date"
                            );


                    Plan plan =
                            new Plan(
                                    planId,
                                    name,
                                    startDate,
                                    endDate,
                                    null
                            );


                    loadPlanExercises(
                            connection,
                            plan,
                            exerciseMap
                    );


                    plans.add(plan);
                }
            }
        }


        return plans;
    }


    // =====================================================
    // LOAD PLAN EXERCISES
    // =====================================================

    private void loadPlanExercises(
            Connection connection,
            Plan plan,
            Map<Integer, Exercise> exerciseMap)
            throws SQLException {


        String sql =
                """
                SELECT
                    day_name,
                    exercise_id,
                    duration,
                    sets,
                    reps
                FROM plan_exercises
                WHERE plan_id = ?
                ORDER BY
                    day_name,
                    exercise_id
                """;


        /*
         * Damit wir die PlanDays nicht mehrfach erzeugen,
         * suchen wir immer zuerst nach dem vorhandenen Tag.
         */

        try (PreparedStatement ps =
                     connection.prepareStatement(
                             sql
                     )) {

            ps.setInt(
                    1,
                    plan.getId()
            );


            try (ResultSet rs =
                         ps.executeQuery()) {


                while (rs.next()) {

                    String dayName =
                            rs.getString(
                                    "day_name"
                            );


                    int exerciseId =
                            rs.getInt(
                                    "exercise_id"
                            );


                    double duration =
                            rs.getDouble(
                                    "duration"
                            );


                    int sets =
                            rs.getInt(
                                    "sets"
                            );


                    int reps =
                            rs.getInt(
                                    "reps"
                            );


                    Exercise exercise =
                            exerciseMap.get(
                                    exerciseId
                            );


                    /*
                     * Falls eine Übung aus irgendeinem Grund
                     * nicht mehr existiert, überspringen wir
                     * den Datensatz.
                     */

                    if (exercise == null) {
                        continue;
                    }


                    PlanDay day =
                            plan.getDay(
                                    dayName
                            );


                    if (day == null) {

                        day =
                                new PlanDay(
                                        dayName
                                );

                        plan.addDay(day);
                    }


                    PlanExercise planExercise =
                            new PlanExercise(
                                    exercise,
                                    duration,
                                    sets,
                                    reps
                            );


                    day.addExercise(
                            planExercise
                    );
                }
            }
        }
    }


    // =====================================================
    // DELETE
    // =====================================================

    @Override
    public void delete(
            int userId,
            int planId) throws SQLException {


        Connection connection =
                DatabaseManager
                        .getInstance()
                        .getConnection();


        boolean oldAutoCommit =
                connection.getAutoCommit();


        try {

            connection.setAutoCommit(false);


            // -------------------------------------------------
            // Plan-Exercises löschen
            // -------------------------------------------------

            String deleteExercisesSql =
                    """
                    DELETE FROM plan_exercises
                    WHERE plan_id = ?
                    """;


            try (PreparedStatement ps =
                         connection.prepareStatement(
                                 deleteExercisesSql
                         )) {

                ps.setInt(
                        1,
                        planId
                );

                ps.executeUpdate();
            }


            // -------------------------------------------------
            // Plan löschen
            // -------------------------------------------------

            String deletePlanSql =
                    """
                    DELETE FROM plans
                    WHERE id = ?
                      AND user_id = ?
                    """;


            try (PreparedStatement ps =
                         connection.prepareStatement(
                                 deletePlanSql
                         )) {

                ps.setInt(
                        1,
                        planId
                );

                ps.setInt(
                        2,
                        userId
                );

                ps.executeUpdate();
            }


            connection.commit();

        } catch (SQLException e) {

            connection.rollback();

            throw e;

        } finally {

            connection.setAutoCommit(
                    oldAutoCommit
            );
        }
    }


    // =====================================================
    // DATE HELPER
    // =====================================================

    private Date toSqlDate(
            java.util.Date date) {

        if (date == null) {
            return null;
        }


        if (date instanceof java.sql.Date sqlDate) {
            return sqlDate;
        }


        return new java.sql.Date(
                date.getTime()
        );
    }
}
