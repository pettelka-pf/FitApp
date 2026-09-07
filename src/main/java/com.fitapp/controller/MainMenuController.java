
package com.fitapp.controller;

import com.fitapp.navigation.Navigator;
import javafx.fxml.FXML;

public class MainMenuController implements Controller {

    private Navigator navigator;

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    public void changeView(String fxmlFile) {
        navigator.changeView(fxmlFile);
    }

    @FXML
    public void handleCheckCalories() {
        changeView("caloricIntake.fxml");
    }

    @FXML
    public void handleAddExercise() { changeView("addExercise.fxml");
    }


    @FXML
    public void handleMakePlan() {changeView("makeplan.fxml");
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
      changeView("stepCounterTest.fxml");
    }

/*
    @FXML
    public void handleStepCounter2() {


        if (navigator == null) {
            System.out.println("NAVIGATOR IST NULL!");
        }

        changeView("stepCounterTest.fxml");
    }
*/
}
