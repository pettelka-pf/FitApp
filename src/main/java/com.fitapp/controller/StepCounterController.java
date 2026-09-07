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


public class StepCounterController implements Controller {

    // -------------------------
    // NAVIGATION
    // -------------------------

    private Navigator navigator;

    private static final int DEFAULT_STEP_GOAL = 10000;

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

    private StepTracker stepTracker;

    private final StepRepository stepDB = new StepDatabase();


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
    private TextField stepsField;

    @FXML
    private TextField remainingField;

    @FXML
    private Label stepOverflowLabel;


    // -------------------------
    // INITIALIZE
    // -------------------------

    /**
     * Lädt das Ziel und die heute bereits gelaufenen
     * Schritte aus der Datenbank.
     */
    @FXML
    public void initialize() {

        // Hintergrundbild an Fenstergröße anpassen.
        BackgroundImageHelper.setup(
                rootPane,
                backgroundImage
        );

        stepOverflowLabel.setVisible(false);

        if (!Session.isLoggedIn()) {

            showMessage("No user logged in.");

            loading = false;

            return;
        }

        int userId = Session.getUserId();
        LocalDate today = LocalDate.now();

        Task<int[]> task = new Task<>() {

            @Override
            protected int[] call() throws Exception {

                return new int[]{
                        stepDB.getGoal(userId),
                        stepDB.getStepsToday(userId, today)
                };
            }
        };


        task.setOnSucceeded(event -> {

            int[] values = task.getValue();

            restoreTracker(
                    values[0],
                    values[1]
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
     * Stellt den StepTracker mit den gespeicherten
     * Werten wieder her.
     */
    private void restoreTracker(
            int goal,
            int steps
    ) {

        stepTracker = new StepTracker(
                goal > 0
                        ? goal
                        : DEFAULT_STEP_GOAL
        );

        stepTracker.setSteps(steps);

        bindRemaining();

        goalField.setPromptText(
                "Current goal: "
                        + stepTracker
                        .getDailyGoal()
                        .get()
        );

        /*
         * Nach dem Laden direkt den aktuellen Zustand
         * anzeigen.
         *
         * Das ist wichtig, falls das Ziel bereits
         * erreicht oder überschritten wurde.
         */
        updateRemainingFieldColor();
        updateStepOverflowMessage();
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
                        "Step goal must be greater than 0."
                );

                return;
            }


            if (stepTracker == null) {

                stepTracker = new StepTracker(goal);

                bindRemaining();

            } else {

                /*
                 * Nur das Ziel ändern.
                 *
                 * Die bereits gelaufenen Schritte
                 * bleiben erhalten.
                 */
                stepTracker
                        .getDailyGoal()
                        .set(goal);
            }


            /*
             * Farbe nach Änderung des Ziels
             * aktualisieren.
             */
            updateRemainingFieldColor();


            /*
             * Prüfen, ob die bereits gelaufenen
             * Schritte das neue Ziel überschreiten.
             */
            updateStepOverflowMessage();


            // Ziel speichern.
            persist(
                    () -> stepDB.setGoal(
                            Session.getUserId(),
                            goal
                    ),
                    "Could not save the goal."
            );

        } catch (NumberFormatException e) {

            showMessage(
                    "Please enter a valid step goal."
            );
        }
    }


    // -------------------------
    // ADD STEPS
    // -------------------------

    @FXML
    public void handleAddingSteps() {

        if (loading) {

            showMessage(
                    "Loading, please wait..."
            );

            return;
        }


        if (stepTracker == null) {

            showMessage(
                    "Please set a goal first."
            );

            return;
        }


        try {

            int steps = Integer.parseInt(
                    stepsField.getText()
            );


            /*
             * Schritte hinzufügen.
             *
             * Das Ziel stellt KEINE Obergrenze dar.
             * Es kann also beliebig weitergezählt werden.
             */
            stepTracker.addSteps(steps);


            // Eingabefeld leeren.
            stepsField.clear();


            /*
             * Remaining-Feld aktualisieren.
             *
             * Unter Ziel:
             *      ROT
             *
             * Ziel erreicht:
             *      GRÜN
             *
             * Ziel überschritten:
             *      GRÜN
             */
            updateRemainingFieldColor();


            /*
             * Meldung aktualisieren.
             *
             * Sobald die tatsächliche Schrittzahl
             * größer als das Ziel ist, wird die
             * Meldung mit der tatsächlichen
             * Schrittzahl angezeigt.
             */
            updateStepOverflowMessage();


            // Schritte speichern.
            persist(
                    () -> stepDB.addSteps(
                            Session.getUserId(),
                            steps,
                            LocalDate.now()
                    ),
                    "Could not save the entry."
            );

        } catch (NegativeStepsException e) {

            showMessage(
                    "Steps must be a positive number!"
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


        if (stepTracker != null) {

            stepTracker.reset();
        }


        stepsField.setText("");


        /*
         * Nach dem Reset:
         *
         * Remaining = Ziel
         * Farbe = ROT
         * Meldung = aus
         */
        updateRemainingFieldColor();

        updateStepOverflowMessage();


        // Datenbank ebenfalls zurücksetzen.
        persist(
                () -> stepDB.resetSteps(
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
    // REMAINING STEPS
    // -------------------------

    /**
     * Bindet die Anzeige der verbleibenden Schritte.
     *
     * Der StepTracker sorgt dafür, dass die Anzeige
     * bei 0 stehen bleibt, sobald das Ziel erreicht
     * wurde.
     */
    private void bindRemaining() {

        remainingField.textProperty().bind(
                stepTracker
                        .remainingStepsProperty()
                        .asString()
        );
    }


    /**
     * Ändert die Hintergrundfarbe des Remaining-Feldes.
     *
     * remaining > 0:
     *     Ziel noch nicht erreicht -> ROT
     *
     * remaining == 0:
     *     Ziel erreicht -> GRÜN
     *
     * Da remainingSteps im StepTracker auf mindestens
     * 0 begrenzt wird, bleibt das Feld auch bei einer
     * Überschreitung GRÜN.
     */
    private void updateRemainingFieldColor() {

        if (stepTracker == null) {
            return;
        }


        int remaining =
                stepTracker
                        .remainingStepsProperty()
                        .get();


        if (remaining > 0) {

            // Ziel noch nicht erreicht.
            remainingField.setStyle(
                    "-fx-control-inner-background: #FF7F7F;" +
                            "-fx-text-fill: black;"
            );

        } else {

            // Ziel erreicht oder überschritten.
            remainingField.setStyle(
                    "-fx-control-inner-background: #90EE90;" +
                            "-fx-text-fill: black;"
            );
        }
    }


    // -------------------------
    // STEP OVERFLOW MESSAGE
    // -------------------------

    /**
     * Zeigt eine Meldung an, sobald das Schrittziel
     * überschritten wurde.
     *
     * Anders als remainingStepsProperty() verwenden
     * wir hier currentStepsProperty(), weil das
     * Remaining-Feld bei 0 bleiben soll.
     *
     * Dadurch kennen wir weiterhin die tatsächliche
     * Anzahl der gelaufenen Schritte.
     */
    private void updateStepOverflowMessage() {

        if (stepTracker == null) {
            return;
        }


        int currentSteps =
                stepTracker
                        .currentStepsProperty()
                        .get();

        int goal =
                stepTracker
                        .getDailyGoal()
                        .get();


        if (currentSteps > goal) {

            stepOverflowLabel.setText(
                    "Lot of Steps taken! You walked "
                            + currentSteps
                            + " steps today."
            );

            stepOverflowLabel.setVisible(true);

        } else {

            stepOverflowLabel.setVisible(false);
        }
    }


    // -------------------------
    // DATABASE / BACKGROUND
    // -------------------------

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


    private void runInBackground(Task<?> task) {

        Thread thread = new Thread(task);

        thread.setDaemon(true);

        thread.start();
    }


    // -------------------------
    // ERROR MESSAGE
    // -------------------------

    private void showMessage(String message) {

        stepOverflowLabel.setText(message);

        stepOverflowLabel.setVisible(true);
    }


    // -------------------------
    // DATABASE ACTION
    // -------------------------

    @FunctionalInterface
    private interface DatabaseAction {

        void run() throws Exception;
    }
}
