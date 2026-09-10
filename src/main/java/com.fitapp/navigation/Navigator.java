package com.fitapp.navigation;

import com.fitapp.controller.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Navigator {

    // Zentrale Standardgröße der Anwendung
    private static final double DEFAULT_WIDTH = 800;
    private static final double DEFAULT_HEIGHT = 800;

    private final Stage stage;

    public Navigator(Stage stage) {
        this.stage = stage;

        // Startgröße des Fensters
        stage.setWidth(DEFAULT_WIDTH);
        stage.setHeight(DEFAULT_HEIGHT);

        // Verhindert, dass das Fenster kleiner als
        // die gewünschte Grundgröße gezogen wird.
        stage.setMinWidth(DEFAULT_WIDTH);
        stage.setMinHeight(DEFAULT_HEIGHT);
    }

    public void changeView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/" + fxmlFile)
            );

            Parent root = loader.load();

            // Navigator automatisch in den Controller injizieren
            Object controller = loader.getController();

            if (controller instanceof Controller ci) {
                ci.setNavigator(this);
            }

            // Neue Scene setzen
            stage.setScene(new Scene(root));
            stage.show();

            switch (fxmlFile) {
                case "mainMenu.fxml" ->
                        stage.setTitle("Menu");

                case "login.fxml" ->
                        stage.setTitle("Login");

                case "caloricIntake.fxml" ->
                        stage.setTitle("Caloric Intake");

                case "register.fxml" ->
                        stage.setTitle("Registrieren");

                case "stepCounter.fxml" ->
                        stage.setTitle("Step Counter");

                // Christian: Fenstertitel fuer die neue Statistik-Seite.
                case "statistics.fxml" ->
                        stage.setTitle("StepCounter Statistik");

                case "makeplan.fxml" ->
                        stage.setTitle("Make Plan");

                case "addExercise.fxml" ->
                        stage.setTitle("Add Exercise");

                default ->
                        stage.setTitle("App");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
