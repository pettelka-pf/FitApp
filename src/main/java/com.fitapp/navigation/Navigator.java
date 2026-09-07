package com.fitapp.navigation;

import com.fitapp.controller.Controller; // your existing controller interface
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Navigator {

    private Stage stage;

    public Navigator(Stage stage) {
        this.stage = stage;
    }

    public void changeView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
            Parent root = loader.load();

            // Automatic injection of this Navigator into controller
            Object controller = loader.getController();
            if (controller instanceof Controller ci) {
                ci.setNavigator(this);
            }

            // Set new scene
            stage.setScene(new Scene(root));
            stage.show();

            switch (fxmlFile) {
                case "mainMenu.fxml" -> stage.setTitle("Menu");
                case "login.fxml" -> stage.setTitle("Login");
                case "caloricIntake.fxml" -> stage.setTitle("Caloric Intake");
                case "register.fxml" -> stage.setTitle("Registrieren");
                case "stepCounterTest.fxml" -> stage.setTitle("Step Counter");
                // Christian: Fenstertitel fuer die neue Statistik-Seite.
                case "statistics.fxml" -> stage.setTitle("StepCounter Statistik");
                case "makeplan.fxml" -> stage.setTitle("Make Plan");
                case "addExercise.fxml" -> stage.setTitle("Add Exercise");
                default -> stage.setTitle("App");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


