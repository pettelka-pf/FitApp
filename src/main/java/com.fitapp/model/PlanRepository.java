package com.fitapp.model;

import java.sql.SQLException;
import java.util.List;

public interface PlanRepository {

    /**
     * Speichert einen Trainingsplan.
     *
     * Wenn der Plan noch keine Datenbank-ID besitzt,
     * wird ein INSERT durchgeführt.
     *
     * Gibt die ID des Plans zurück.
     */
    int save(int userId, Plan plan) throws SQLException;


    /**
     * Lädt alle Trainingspläne eines Users.
     */
    List<Plan> findByUser(int userId) throws SQLException;


    /**
     * Löscht einen Trainingsplan.
     */
    void delete(int userId, int planId) throws SQLException;
}

