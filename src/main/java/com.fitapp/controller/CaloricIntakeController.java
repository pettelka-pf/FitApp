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

    private static final int DEFAULT_CALORIE_GOAL = 2000;

    // true, solange initialize() noch aus der Datenbank liest.
    private boolean loading = true;


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

    // Zugriff auf die Datenbank.
    private final CalorieRepository calorieDB =
            new CalorieDatabase();

    // Heute verbrannte Kalorien.
    // Wird für den Fall eines neuen Ziels benötigt.
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
    // INITIALIZE
    // -------------------------

    /**
     * Lädt das Ziel, die heute gegessenen Kalorien
     * und die heute verbrannten Kalorien aus der Datenbank.
     */
    @FXML
    public void initialize() {

        // Hintergrundbild an Fenstergröße anpassen.
        BackgroundImageHelper.setup(
                rootPane,
                backgroundImage
        );

        caloriesOverflowLabel.setVisible(false);

        if (!Session.isLoggedIn()) {

            showMessage(
                    "No user logged in."
            );

            loading = false;

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

            loading = false;
        });


        task.setOnFailed(event -> {

            showMessage(
                    "Could not load saved data."
            );

            loading = false;
        });


        runInBackground(task);
    }


    // -------------------------
    // RESTORE TRACKER
    // -------------------------

    /**
     * Stellt den CaloriesTracker mit den gespeicherten
     * Werten wieder her.
     */
    private void restoreTracker(
            int goal,
            int eaten,
            int burned
    ) {

        burnedToday = burned;

        calTra = new CaloriesTracker(
                goal > 0
                        ? goal
                        : DEFAULT_CALORIE_GOAL
        );

        calTra.setBurned(burned);

        bindRemaining();

        goalField.setPromptText(
                "Current goal: "
                        + calTra
                        .getDailyLimit()
                        .get()
        );


        /*
         * Bereits gegessene Kalorien wiederherstellen.
         *
         * Eine Überschreitung des Tagesziels ist erlaubt.
         * Der Remaining-Wert wird trotzdem bei 0 begrenzt.
         */
        if (eaten > 0) {

            try {

                calTra.addCalories(eaten);

            } catch (NegativeCaloriesException e) {

                showMessage(
                        "Could not restore calorie data."
                );

                return;
            }
        }


        /*
         * Nach dem Laden direkt den aktuellen Zustand
         * anzeigen.
         */
        updateRemainingFieldColor();
        updateCaloriesOverflowMessage();
    }


    // -------------------------
    // SET GOAL
    // -------------------------

    @FXML
    public void handleSetGoal(ActionEvent event) {

        if (loading) {

            showMessage(
                    "Loading, please wait..."
            );

            return;
        }


        try {

            int goal = Integer.parseInt(
                    goalField.getText()
            );


            if (goal <= 0) {

                showMessage(
                        "Calorie goal must be greater than 0."
                );

                return;
            }


            if (calTra == null) {

                calTra = new CaloriesTracker(goal);

                calTra.setBurned(burnedToday);

                bindRemaining();

            } else {

                /*
                 * Nur das Ziel ändern.
                 *
                 * Die bereits gegessenen Kalorien
                 * bleiben erhalten.
                 */
                calTra
                        .getDailyLimit()
                        .set(goal);
            }


            /*
             * Farbe nach Änderung des Ziels
             * aktualisieren.
             */
            updateRemainingFieldColor();


            /*
             * Prüfen, ob die bereits gegessenen
             * Kalorien das neue verfügbare Budget
             * überschreiten.
             */
            updateCaloriesOverflowMessage();


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

        if (loading) {

            showMessage(
                    "Loading, please wait..."
            );

            return;
        }


        if (calTra == null) {

            showMessage(
                    "Please set a calorie goal first."
            );

            return;
        }


        try {

            int calories = Integer.parseInt(
                    caloriesField.getText()
            );


            /*
             * Kalorien hinzufügen.
             *
             * Das Ziel stellt KEINE Obergrenze dar.
             * Es kann also beliebig weitergegessen werden.
             */
            calTra.addCalories(calories);


            // Eingabefeld leeren.
            caloriesField.clear();


            /*
             * Remaining-Feld aktualisieren.
             *
             * Unter dem verfügbaren Budget:
             *     positiver Wert
             *
             * Ziel erreicht:
             *     0
             *
             * Ziel überschritten:
             *     0
             *
             * Der Wert wird im CaloriesTracker
             * auf mindestens 0 begrenzt.
             */
            updateRemainingFieldColor();


            /*
             * Prüfen, ob das verfügbare Kalorienbudget
             * überschritten wurde.
             *
             * Die Meldung verwendet die tatsächlichen
             * aufgenommenen Kalorien und nicht den
             * Remaining-Wert.
             */
            updateCaloriesOverflowMessage();


            // Mahlzeit speichern.
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

            showMessage(
                    "Calories must be a positive number!"
            );

        } catch (NumberFormatException e) {

            showMessage(
                    "Please enter a valid number."
            );
        }
    }


    // -------------------------
    // RESET
    // -------------------------

    @FXML
    public void handleReset(ActionEvent event) {

        if (loading) {

            showMessage(
                    "Loading, please wait..."
            );

            return;
        }


        if (calTra != null) {

            calTra.reset();
        }


        caloriesField.clear();


        /*
         * Nach dem Reset:
         *
         * Gegessene Kalorien = 0
         * Remaining = Tageslimit + verbrannte Kalorien
         * Meldung = aus
         */
        updateRemainingFieldColor();

        updateCaloriesOverflowMessage();


        // Datenbank ebenfalls zurücksetzen.
        persist(
                () -> calorieDB.resetMeals(
                        Session.getUserId(),
                        LocalDate.now()
                ),
                "Could not reset the entries."
        );
    }


    // -------------------------
    // BACK TO MENU
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
     *
     * Der CaloriesTracker sorgt dafür, dass die Anzeige
     * bei 0 stehen bleibt, sobald das verfügbare Budget
     * erreicht oder überschritten wurde.
     */
    private void bindRemaining() {

        remainingField.textProperty().bind(
                calTra
                        .remainingCaloriesProperty()
                        .asString()
        );
    }


    /**
     * Ändert die Hintergrundfarbe des Remaining-Feldes.
     *
     * remaining > 0:
     *     Kalorienbudget noch nicht erreicht
     *
     * remaining == 0:
     *     Budget erreicht oder überschritten
     *
     * Die eigentliche Farblogik bleibt dabei wie
     * in deinem bisherigen Controller:
     *
     * Grün = noch Kalorien verfügbar
     * Rot   = Limit erreicht/überschritten
     */
    private void updateRemainingFieldColor() {

        if (calTra == null) {
            return;
        }


        int remaining =
                calTra
                        .remainingCaloriesProperty()
                        .get();


        if (remaining > 0) {

            // Noch Kalorien verfügbar.
            remainingField.setStyle(
                    "-fx-control-inner-background: #90EE90;" +
                            "-fx-text-fill: black;"
            );

        } else {

            // Limit erreicht oder überschritten.
            remainingField.setStyle(
                    "-fx-control-inner-background: #FF7F7F;" +
                            "-fx-text-fill: black;"
            );
        }
    }


    // -------------------------
    // CALORIE OVERFLOW MESSAGE
    // -------------------------

    /**
     * Zeigt eine Meldung an, sobald das verfügbare
     * Kalorienbudget überschritten wurde.
     *
     * Wichtig:
     *
     * remainingCaloriesProperty() wird hierfür NICHT
     * verwendet, weil dieser Wert bei 0 gedeckelt wird.
     *
     * Stattdessen wird die tatsächlich aufgenommene
     * Kalorienmenge aus consumed gelesen.
     *
     * Verbrannte Kalorien erhöhen das verfügbare
     * Tagesbudget.
     */
    private void updateCaloriesOverflowMessage() {

        if (calTra == null) {
            return;
        }


        int consumed =
                calTra
                        .getConsumed()
                        .get();


        int available =
                calTra
                        .getDailyLimit()
                        .get()
                        + calTra
                        .getBurned()
                        .get();


        if (consumed > available) {

            caloriesOverflowLabel.setText(
                    "You consumed "
                            + consumed
                            + " calories today."
            );

            caloriesOverflowLabel.setVisible(true);

        } else {

            caloriesOverflowLabel.setVisible(false);
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
     * Startet den Task in einem Daemon-Thread.
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

    @FunctionalInterface
    private interface DatabaseAction {

        void run() throws Exception;
    }
}
