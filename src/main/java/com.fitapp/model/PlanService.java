package com.fitapp.model;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class PlanService {

    private final PlanRepository planRepository;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public PlanService() {

        this.planRepository =
                new PlanDatabase();
    }


    // =====================================================
    // PLAN CREATION
    // =====================================================

    public Plan createPlan(
            String name,
            Date startDate,
            Date endDate) {

        return new Plan(
                0,
                name,
                startDate,
                endDate,
                null
        );
    }


    // =====================================================
    // SAVE PLAN
    // =====================================================

    public int savePlan(
            int userId,
            Plan plan) throws SQLException {

        if (!isValidPlan(plan)) {

            throw new IllegalArgumentException(
                    "Invalid training plan."
            );
        }


        return planRepository.save(
                userId,
                plan
        );
    }


    // =====================================================
    // LOAD PLANS
    // =====================================================

    public List<Plan> getPlansForUser(
            int userId) throws SQLException {

        return planRepository.findByUser(
                userId
        );
    }


    // =====================================================
    // DELETE PLAN
    // =====================================================

    public void deletePlan(
            int userId,
            int planId) throws SQLException {

        planRepository.delete(
                userId,
                planId
        );
    }


    // =====================================================
    // DAY MANAGEMENT
    // =====================================================

    public void addDayToPlan(
            Plan plan,
            String dayName) {

        if (plan == null
                || dayName == null) {

            return;
        }


        if (plan.getDay(dayName) != null) {
            return;
        }


        PlanDay day =
                new PlanDay(dayName);


        plan.addDay(day);
    }


    public PlanDay getDayFromPlan(
            Plan plan,
            String dayName) {

        if (plan == null) {
            return null;
        }


        return plan.getDay(dayName);
    }


    // =====================================================
    // EXERCISE MANAGEMENT
    // =====================================================

    public void addExerciseToDay(
            Plan plan,
            String dayName,
            Exercise exercise,
            double duration,
            int sets,
            int reps) {

        if (plan == null
                || exercise == null
                || dayName == null) {

            return;
        }


        PlanDay day =
                plan.getDay(dayName);


        if (day == null) {

            day =
                    new PlanDay(dayName);

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


    public void removeExerciseFromDay(
            Plan plan,
            String dayName,
            int exerciseId) {

        if (plan == null
                || dayName == null) {

            return;
        }


        PlanDay day =
                plan.getDay(dayName);


        if (day != null) {

            day.removeExercise(
                    exerciseId
            );
        }
    }


    // =====================================================
    // CALCULATIONS
    // =====================================================

    public double calculatePlanCalories(
            Plan plan) {

        if (plan == null) {
            return 0;
        }


        return plan.getTotalCalories();
    }


    public double calculatePlanDuration(
            Plan plan) {

        if (plan == null) {
            return 0;
        }


        return plan.getTotalDuration();
    }


    // =====================================================
    // VALIDATION
    // =====================================================

    public boolean isValidPlan(
            Plan plan) {

        if (plan == null) {
            return false;
        }


        if (plan.getName() == null
                || plan.getName()
                .trim()
                .isEmpty()) {

            return false;
        }


        if (plan.getStartDate() == null) {
            return false;
        }


        if (plan.getEndDate() == null) {
            return false;
        }


        if (plan.getEndDate()
                .before(plan.getStartDate())) {

            return false;
        }


        return true;
    }
}
