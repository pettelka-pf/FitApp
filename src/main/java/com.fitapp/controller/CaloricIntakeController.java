package com.fitapp.controller;

import com.fitapp.model.*;
import com.fitapp.navigation.Navigator;
import com.fitapp.util.BackgroundImageHelper;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;


public class CaloricIntakeController implements Controller {

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
    // MODEL
    // -------------------------

    private CaloriesTracker calTra;

    // Zugriff auf die Datenbank, damit die Werte einen Neustart überleben.
    private final CalorieRepository calorieDB = new CalorieDatabase();

    // Heute verbrannte Kalorien, gemerkt für den Fall eines neuen Ziels.
    private int burnedToday;


    // -------------------------
    // FXML FIELDS
    // -------------------------

    @FXML
    private StackPane rootPane;

    @FXML
    private ImageView backgroundImage;

    @FXML
    private TextField goalField;

    @FXML
    private TextField caloriesField;

    @FXML
    private TextField remainingField;

    @FXML
    private Label caloriesOverflowLabel;


    // -------------------------
    // CONSTRUCTOR
    // -------------------------

    public CaloricIntakeController() {
    }


    // -------------------------
    // INITIALIZE
    // -------------------------

    /**
     * Wird vom FXMLLoader aufgerufen.
     *
     * Lädt das gespeicherte Ziel sowie die heute bereits
     * gegessenen und verbrannten Kalorien.
     */
    @FXML
    public void initialize() {

        caloriesOverflowLabel.setVisible(false);

        // Hintergrundbild automatisch an die Fenstergröße anpassen.
        BackgroundImageHelper.setup(
                rootPane,
                backgroundImage
        );

        if (!Session.isLoggedIn()) {
            showMessage("No user logged in.");
            return;
        }

        int userId = Session.getUserId();
        LocalDate today = LocalDate.now();

        Task<int[]> task = new Task<>() {
            @Override
            protected int[] call() throws Exception {
                return new int[]{
                        calorieDB.getGoal(userId),
                        calorieDB.getEatenToday(userId, today),
                        calorieDB.getBurnedToday(userId, today)
                };
            }
        };

        task.setOnSucceeded(event -> {

            int[] values = task.getValue();

            restoreTracker(
                    values[0],
                    values[1],
                    values[2]
            );
        });

        task.setOnFailed(event ->
                showMessage("Could not load saved data.")
        );

        runInBackground(task);
    }


    // -------------------------
    // RESTORE TRACKER
    // -------------------------

    /**
     * Baut den Tracker mit dem gespeicherten Ziel auf und
     * spielt die bereits gegessenen Kalorien nach.
     */
    private void restoreTracker(
            int goal,
            int eaten,
            int burned
    ) {

        burnedToday = burned;

        calTra = new CaloriesTracker(goal);
        calTra.setBurned(burned);

        bindRemaining();

        goalField.setPromptText(
                "Current goal: " + goal
        );

        if (eaten <= 0) {
            updateRemainingFieldColor();
            return;
        }

        try {

            calTra.addCalories(eaten);

            // Farbe entsprechend dem aktuellen Stand setzen.
            updateRemainingFieldColor();

        } catch (NegativeCaloriesException |
                 CalorieLimitExceededException e) {

            showMessage(
                    "Exceeded daily calorie limit!"
            );

            updateRemainingFieldColor();
        }
    }


    // -------------------------
    // SET GOAL
    // -------------------------

    @FXML
    public void handleSetGoal(ActionEvent event) {

        try {

            int goal = Integer.parseInt(
                    goalField.getText()
            );

            if (calTra == null) {

                calTra = new CaloriesTracker(goal);

                calTra.setBurned(burnedToday);

                bindRemaining();

            } else {

                /*
                 * Nur das Ziel ändern.
                 * Die bereits gegessenen Kalorien bleiben erhalten.
                 */
                calTra.getDailyLimit().set(goal);
            }

            caloriesOverflowLabel.setVisible(false);

            // Farbe nach Änderung des Ziels aktualisieren.
            updateRemainingFieldColor();

            // Ziel speichern.
            persist(
                    () -> calorieDB.setGoal(
                            Session.getUserId(),
                            goal
                    ),
                    "Could not save the goal."
            );

        } catch (NumberFormatException e) {

            showMessage(
                    "Please enter a valid calorie goal."
            );
        }
    }


    // -------------------------
    // ADD CALORIES
    // -------------------------

    @FXML
    public void handleAddingCalories() {

        if (calTra == null) {

            caloriesOverflowLabel.setText(
                    "Please set a calorie goal first."
            );

            caloriesOverflowLabel.setVisible(true);

            return;
        }

        try {

            int calories = Integer.parseInt(
                    caloriesField.getText()
            );

            // Kalorien hinzufügen
            calTra.addCalories(calories);

            // Anzeige aktualisieren
            remainingField.textProperty().bind(
                    calTra.remainingCaloriesProperty().asString()
            );

            // Farbe und Fehlermeldung aktualisieren
            updateRemainingFieldColor();

            /*
             * Prüfen, ob das Kalorienlimit überschritten wurde.
             *
             * remaining > 0  -> noch unter dem Ziel
             * remaining == 0 -> Ziel genau erreicht
             * remaining < 0  -> Ziel überschritten
             */
            int remaining =
                    calTra.remainingCaloriesProperty().get();

            if (remaining < 0) {

                caloriesOverflowLabel.setText(
                        "Exceeded daily calorie limit!"
                );

                caloriesOverflowLabel.setVisible(true);

            } else {

                caloriesOverflowLabel.setVisible(false);
            }


            // Mahlzeit speichern
            persist(
                    () -> calorieDB.addMeal(
                            Session.getUserId(),
                            "Meal",
                            calories,
                            LocalDate.now()
                    ),
                    "Could not save the entry."
            );

        } catch (NegativeCaloriesException e) {

            caloriesOverflowLabel.setText(
                    "Calories must be a positive number!"
            );

            caloriesOverflowLabel.setVisible(true);

        } catch (NumberFormatException e) {

            caloriesOverflowLabel.setText(
                    "Please enter a valid number."
            );

            caloriesOverflowLabel.setVisible(true);
        }
    }



    // -------------------------
    // RESET
    // -------------------------

    @FXML
    public void handleReset(ActionEvent event) {

        if (calTra != null) {

            calTra.reset();
        }

        caloriesField.setText("");

        caloriesOverflowLabel.setVisible(false);

        /*
         * Nach dem Reset sind wieder alle Zielkalorien verfügbar.
         * Deshalb wird das Feld wieder grün.
         */
        updateRemainingFieldColor();

        // Auch die Datenbank zurücksetzen.
        persist(
                () -> calorieDB.resetMeals(
                        Session.getUserId(),
                        LocalDate.now()
                ),
                "Could not reset the entries."
        );
    }


    // -------------------------
    // BACK
    // -------------------------

    @FXML
    public void handleBackToMenu(ActionEvent event) {

        changeView("mainMenu.fxml");
    }


    // -------------------------
    // REMAINING CALORIES
    // -------------------------

    /**
     * Bindet die Anzeige der verbleibenden Kalorien.
     */
    private void bindRemaining() {

        remainingField.textProperty().bind(
                calTra.remainingCaloriesProperty().asString()
        );

        updateRemainingFieldColor();
    }


    /**
     * Ändert die Hintergrundfarbe des Remaining-Feldes
     * abhängig davon, ob das Kalorienziel bereits erreicht wurde.
     *
     * Grün:
     *     Noch Kalorien übrig.
     *
     * Rot:
     *     Ziel erreicht oder überschritten.
     */
    private void updateRemainingFieldColor() {

        if (calTra == null) {
            return;
        }

        int remaining =
                calTra.remainingCaloriesProperty().get();

        if (remaining > 0) {

            // Noch Kalorien bis zum Ziel verfügbar.
            remainingField.setStyle(
                    "-fx-control-inner-background: #90EE90;" +
                            "-fx-text-fill: black;"
            );

        } else {

            // Ziel erreicht.
            remainingField.setStyle(
                    "-fx-control-inner-background: #FF7F7F;" +
                            "-fx-text-fill: black;"
            );
        }
    }


    // -------------------------
    // DATABASE / BACKGROUND
    // -------------------------

    /**
     * Schreibt im Hintergrund in die Datenbank,
     * damit die Oberfläche nicht einfriert.
     */
    private void persist(
            DatabaseAction action,
            String errorMessage
    ) {

        Task<Void> task = new Task<>() {

            @Override
            protected Void call() throws Exception {

                action.run();

                return null;
            }
        };

        task.setOnFailed(event ->
                showMessage(errorMessage)
        );

        runInBackground(task);
    }


    /**
     * Daemon-Thread, damit ein hängender Zugriff
     * das Schließen der Anwendung nicht blockiert.
     */
    private void runInBackground(Task<?> task) {

        Thread thread = new Thread(task);

        thread.setDaemon(true);

        thread.start();
    }


    // -------------------------
    // ERROR MESSAGE
    // -------------------------

    private void showMessage(String message) {

        caloriesOverflowLabel.setText(message);

        caloriesOverflowLabel.setVisible(true);
    }


    // -------------------------
    // DATABASE ACTION
    // -------------------------

    /**
     * Alles, was eine Exception werfen darf
     * und nichts zurückgibt.
     */
    @FunctionalInterface
    private interface DatabaseAction {

        void run() throws Exception;
    }
}
