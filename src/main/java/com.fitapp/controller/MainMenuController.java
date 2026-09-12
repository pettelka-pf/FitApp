package com.fitapp.controller;

import com.fitapp.navigation.Navigator;
import com.fitapp.util.BackgroundImageHelper;
import com.fitapp.model.Session;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;


public class MainMenuController implements Controller {

    // -------------------------
    // FXML FIELDS
    // -------------------------

    @FXML
    private StackPane rootPane;

    @FXML
    private ImageView backgroundImage;


    // -------------------------
    // NAVIGATION
    // -------------------------

    private Navigator navigator;

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    public void changeView(String fxmlFile) {
        navigator.changeView(fxmlFile);
    }


    // -------------------------
    // INITIALIZE
    // -------------------------

    @FXML
    public void initialize() {

        // Hintergrundbild automatisch an die Größe
        // des Fensters anpassen.
        //
        // Das Bild:
        // - füllt die komplette Fläche
        // - behält sein Seitenverhältnis
        // - wird nicht verzerrt
        // - wird bei abweichendem Seitenverhältnis beschnitten

        BackgroundImageHelper.setup(
                rootPane,
                backgroundImage
        );
    }


    // -------------------------
    // MENU ACTIONS
    // -------------------------

    @FXML
    public void handleCheckCalories() {
        changeView("caloricIntake.fxml");
    }


    @FXML
    public void handleAddExercise() {
        changeView("addExercise.fxml");
    }


    @FXML
    public void handleMakePlan() {
        changeView("makeplan.fxml");
    }


    @FXML
    public void handleViewStatistics() {
        // Christian: oeffnet die neue Statistik-Seite.
        changeView("statistics.fxml");
        System.out.println("View Statistics clicked");
    }


    @FXML
    public void handleStepCounter() {
        System.out.println("Step Counter clicked");
        changeView("stepCounter.fxml");
    }


    @FXML
    public void handleLogout() {

        Session.logout();

        changeView("login.fxml");
    }


}
