package com.fitapp.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Plan {

    // -------------------------
    // ATTRIBUTES
    // -------------------------

    private int id;
    private String name;
    private Date startDate;
    private Date endDate;

    private List<PlanDay> days;


    // -------------------------
    // CONSTRUCTOR
    // -------------------------

    public Plan(
            int id,
            String name,
            Date startDate,
            Date endDate,
            List<PlanDay> days) {

        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;

        this.days = (days != null)
                ? days
                : new ArrayList<>();

        // Bereits vorhandene Tage direkt sortieren
        sortDays();
    }


    // -------------------------
    // GETTERS
    // -------------------------

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public List<PlanDay> getDays() {
        return days;
    }


    // -------------------------
    // DAY MANAGEMENT
    // -------------------------

    public void addDay(PlanDay day) {

        if (day != null) {

            days.add(day);

            // Nach jedem Hinzufügen automatisch sortieren
            sortDays();
        }
    }


    public PlanDay getDay(String dayName) {

        for (PlanDay day : days) {

            if (day.getDayName().equalsIgnoreCase(dayName)) {
                return day;
            }
        }

        return null;
    }


    public void removeDay(String dayName) {

        days.removeIf(
                day ->
                        day.getDayName()
                                .equalsIgnoreCase(dayName)
        );
    }


    // -------------------------
    // SORT DAYS
    // -------------------------

    /**
     * Sortiert die Trainingstage automatisch von
     * Montag bis Sonntag.
     */
    private void sortDays() {

        days.sort(
                (day1, day2) ->
                        Integer.compare(
                                getDayOrder(day1.getDayName()),
                                getDayOrder(day2.getDayName())
                        )
        );
    }


    /**
     * Gibt jedem Wochentag eine feste Reihenfolge.
     */
    private int getDayOrder(String dayName) {

        if (dayName == null) {
            return 99;
        }

        switch (dayName.toLowerCase()) {

            case "montag":
                return 1;

            case "dienstag":
                return 2;

            case "mittwoch":
                return 3;

            case "donnerstag":
                return 4;

            case "freitag":
                return 5;

            case "samstag":
                return 6;

            case "sonntag":
                return 7;

            default:
                return 99;
        }
    }


    // -------------------------
    // CALCULATIONS
    // -------------------------

    public double getTotalCalories() {

        double total = 0;

        for (PlanDay day : days) {

            total += day.getTotalCalories();
        }

        return total;
    }


    public double getTotalDuration() {

        double total = 0;

        for (PlanDay day : days) {

            total += day.getTotalDuration();
        }

        return total;
    }


    // -------------------------
    // NUMBER OF DAYS
    // -------------------------

    public int getNumberOfDays() {

        return days.size();
    }
}
