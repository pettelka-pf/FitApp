package com.fitapp.controller;

import com.fitapp.model.*;
import com.fitapp.navigation.Navigator;
import com.fitapp.util.BackgroundImageHelper;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class PlanController implements Controller {

// =====================================================
// NAVIGATION
// =====================================================

    private Navigator navigator;


    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }


    @Override
    public void changeView(String fxmlFile) {
        navigator.changeView(fxmlFile);
    }


// =====================================================
// SERVICES / MODEL
// =====================================================

    private final PlanService planService = new PlanService();

    private final ExerciseService exerciseService =
            new ExerciseService();


    /*
     * Liste aller Trainingspläne.
     *
     * Jeder neu erstellte Plan wird hier gespeichert.
     */
    private final List<Plan> plans = new ArrayList<>();


    /*
     * Der aktuell ausgewählte Trainingsplan.
     */
    private Plan currentPlan;


// =====================================================
// MAIN WINDOW
// =====================================================

    @FXML
    private StackPane rootPane;

    @FXML
    private ImageView backgroundImage;


// =====================================================
// FXML - PLAN
// =====================================================

    @FXML
    private TextField planNameField;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;


// =====================================================
// FXML - DAYS
// =====================================================

    @FXML
    private ComboBox<String> dayField;

    @FXML
    private ComboBox<PlanDay> daySelector;


// =====================================================
// FXML - EXERCISES
// =====================================================

    @FXML
    private ComboBox<String> exerciseTypeBox;

    @FXML
    private ComboBox<Exercise> exerciseBox;


// =====================================================
// FXML - PLAN EXERCISE DATA
// =====================================================

    @FXML
    private TextField durationField;

    @FXML
    private TextField setsField;

    @FXML
    private TextField repsField;


// =====================================================
// FXML - PLAN OVERVIEW
// =====================================================

    @FXML
    private Label overviewPlanName;

    @FXML
    private Label overviewDates;

    @FXML
    private VBox overviewContainer;

    @FXML
    private Label overviewDuration;

    @FXML
    private Label overviewCalories;


// =====================================================
// FXML - PLAN SELECTOR
// =====================================================

    /*
     * Dropdown zur Auswahl zwischen mehreren Trainingsplänen.
     */
    @FXML
    private ComboBox<Plan> planSelector;


// =====================================================
// INITIALIZE
// =====================================================

    @FXML
    public void initialize() {

        // ---------------------------------------------
        // Background
        // ---------------------------------------------

        BackgroundImageHelper.setup(
                rootPane,
                backgroundImage
        );


        // ---------------------------------------------
        // Exercise categories
        // ---------------------------------------------

        exerciseTypeBox.setItems(
                FXCollections.observableArrayList(
                        "WEIGHT",
                        "CARDIO_RUNNING",
                        "CARDIO_CALISTHENICS"
                )
        );


        // ---------------------------------------------
        // Weekdays
        // ---------------------------------------------

        dayField.setItems(
                FXCollections.observableArrayList(
                        "Montag",
                        "Dienstag",
                        "Mittwoch",
                        "Donnerstag",
                        "Freitag",
                        "Samstag",
                        "Sonntag"
                )
        );


        // ---------------------------------------------
        // Initially empty exercise list
        // ---------------------------------------------

        exerciseBox.setItems(
                FXCollections.observableArrayList()
        );


        // ---------------------------------------------
        // Initially empty day list
        // ---------------------------------------------

        daySelector.setItems(
                FXCollections.observableArrayList()
        );


        // ---------------------------------------------
        // Plan selector
        // ---------------------------------------------

        planSelector.setItems(
                FXCollections.observableArrayList()
        );


        /*
         * Der Name des Plans wird im Dropdown angezeigt.
         *
         * Dadurch muss Plan nicht zwingend eine
         * eigene toString()-Methode besitzen.
         */
        planSelector.setCellFactory(listView -> {

            javafx.scene.control.ListCell<Plan> cell =
                    new javafx.scene.control.ListCell<>() {

                        @Override
                        protected void updateItem(
                                Plan plan,
                                boolean empty) {

                            super.updateItem(plan, empty);

                            if (empty || plan == null) {

                                setText(null);

                            } else {

                                setText(plan.getName());
                            }
                        }
                    };

            return cell;
        });


        /*
         * Auch der aktuell ausgewählte Eintrag
         * wird mit dem Namen des Plans angezeigt.
         */
        planSelector.setButtonCell(
                new javafx.scene.control.ListCell<Plan>() {

                    @Override
                    protected void updateItem(
                            Plan plan,
                            boolean empty) {

                        super.updateItem(plan, empty);

                        if (empty || plan == null) {

                            setText("no plan available");

                        } else {

                            setText(plan.getName());
                        }
                    }
                }
        );



        // ---------------------------------------------
        // Exercise type selection
        // ---------------------------------------------

        exerciseTypeBox.setOnAction(
                event -> handleTypeSelect()
        );


        // ---------------------------------------------
        // Initial overview
        // ---------------------------------------------

        updateOverview();
    }


// =====================================================
// CREATE PLAN
// =====================================================

    @FXML
    public void handleCreatePlan() {

        String planName =
                planNameField.getText().trim();


        // ---------------------------------------------
        // Validation
        // ---------------------------------------------

        if (planName.isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Missing information",
                    "Please enter a plan name."
            );

            return;
        }


        if (startDatePicker.getValue() == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Missing information",
                    "Please select a start date."
            );

            return;
        }


        if (endDatePicker.getValue() == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Missing information",
                    "Please select an end date."
            );

            return;
        }


        if (endDatePicker.getValue()
                .isBefore(startDatePicker.getValue())) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Invalid dates",
                    "The end date cannot be before the start date."
            );

            return;
        }


        // ---------------------------------------------
        // Create new plan
        // ---------------------------------------------

        /*
         * Für die momentan lokale Planverwaltung
         * verwenden wir eine fortlaufende ID.
         */
        int planId = plans.size() + 1;


        Plan newPlan =
                planService.createPlan(
                        planId,
                        planName,
                        java.sql.Date.valueOf(
                                startDatePicker.getValue()
                        ),
                        java.sql.Date.valueOf(
                                endDatePicker.getValue()
                        )
                );


        // ---------------------------------------------
        // Add plan to list
        // ---------------------------------------------

        plans.add(newPlan);


        // ---------------------------------------------
        // Set new plan as current plan
        // ---------------------------------------------

        currentPlan = newPlan;


        // ---------------------------------------------
        // Update plan selector
        // ---------------------------------------------

        refreshPlanSelector();


        /*
         * Den neu erstellten Plan direkt auswählen.
         */
        planSelector.setValue(newPlan);


        // ---------------------------------------------
        // Reset selectors
        // ---------------------------------------------

        daySelector.setItems(
                FXCollections.observableArrayList()
        );


        exerciseBox.setItems(
                FXCollections.observableArrayList()
        );


        // ---------------------------------------------
        // Update overview
        // ---------------------------------------------

        updateOverview();


        // ---------------------------------------------
        // Clear plan input
        // ---------------------------------------------

        planNameField.clear();


        showAlert(
                Alert.AlertType.INFORMATION,
                "Plan created",
                "Training plan \"" +
                        planName +
                        "\" was created successfully."
        );
    }


// =====================================================
// REFRESH PLAN SELECTOR
// =====================================================

    private void refreshPlanSelector() {

        planSelector.setItems(
                FXCollections.observableArrayList(
                        plans
                )
        );
    }


// =====================================================
// SELECT PLAN
// =====================================================

    /**
     * Wird aufgerufen, wenn im Dropdown ein anderer
     * Trainingsplan ausgewählt wird.
     */
    @FXML
    public void handlePlanSelection() {

        Plan selectedPlan =
                planSelector.getValue();


        if (selectedPlan == null) {
            return;
        }


        // ---------------------------------------------
        // Current plan wechseln
        // ---------------------------------------------

        currentPlan = selectedPlan;


        // ---------------------------------------------
        // Tage des Plans laden
        // ---------------------------------------------

        refreshDays();


        // ---------------------------------------------
        // Exercise selection zurücksetzen
        // ---------------------------------------------

        exerciseBox.setItems(
                FXCollections.observableArrayList()
        );

        exerciseTypeBox.getSelectionModel()
                .clearSelection();


        // ---------------------------------------------
        // Update overview
        // ---------------------------------------------

        updateOverview();
    }


// =====================================================
// ADD DAY
// =====================================================

    @FXML
    public void handleAddDay() {

        if (currentPlan == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No plan",
                    "Create or select a training plan first."
            );

            return;
        }


        String dayName =
                dayField.getValue();


        if (dayName == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Missing day",
                    "Please select a day."
            );

            return;
        }


        if (currentPlan.getDay(dayName) != null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Day already exists",
                    dayName +
                            " has already been added."
            );

            return;
        }


        // ---------------------------------------------
        // Add day
        // ---------------------------------------------

        planService.addDayToPlan(
                currentPlan,
                dayName
        );


        // ---------------------------------------------
        // Refresh day selector
        // ---------------------------------------------

        refreshDays();


        // ---------------------------------------------
        // Select new day
        // ---------------------------------------------

        for (PlanDay day :
                daySelector.getItems()) {

            if (day.getDayName()
                    .equalsIgnoreCase(dayName)) {

                daySelector.setValue(day);

                break;
            }
        }


        // ---------------------------------------------
        // Update overview
        // ---------------------------------------------

        updateOverview();


        showAlert(
                Alert.AlertType.INFORMATION,
                "Day added",
                "Day \"" +
                        dayName +
                        "\" was added to the plan."
        );
    }


// =====================================================
// REFRESH DAYS
// =====================================================

    private void refreshDays() {

        if (currentPlan == null) {
            return;
        }


        daySelector.setItems(
                FXCollections.observableArrayList(
                        currentPlan.getDays()
                )
        );
    }


// =====================================================
// SELECT EXERCISE TYPE
// =====================================================

    @FXML
    public void handleTypeSelect() {

        String selectedType =
                exerciseTypeBox.getValue();


        if (selectedType == null) {

            exerciseBox.setItems(
                    FXCollections.observableArrayList()
            );

            return;
        }


        // ---------------------------------------------
        // Get exercises from database
        // ---------------------------------------------

        List<Exercise> allExercises;

        try {

            allExercises =
                    exerciseService.getAllExercises(
                            Session.getUserId()
                    );

        } catch (java.sql.SQLException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Database error",
                    "Exercises could not be loaded."
            );

            return;
        }


        List<Exercise> filteredExercises =
                new ArrayList<>();


        // ---------------------------------------------
        // Filter
        // ---------------------------------------------

        for (Exercise exercise :
                allExercises) {

            if ("WEIGHT".equals(selectedType)
                    && exercise instanceof WeightExercise) {

                filteredExercises.add(exercise);

            } else if (
                    "CARDIO_RUNNING".equals(selectedType)
                            && exercise instanceof CardioRunningExercise) {

                filteredExercises.add(exercise);

            } else if (
                    "CARDIO_CALISTHENICS".equals(selectedType)
                            && exercise instanceof CardioCalisthenicsExercise) {

                filteredExercises.add(exercise);
            }
        }


        // ---------------------------------------------
        // Put exercises into ComboBox
        // ---------------------------------------------

        exerciseBox.setItems(
                FXCollections.observableArrayList(
                        filteredExercises
                )
        );


        exerciseBox.getSelectionModel()
                .clearSelection();


        if (filteredExercises.isEmpty()) {

            System.out.println(
                    "No exercises found for type: "
                            + selectedType
            );
        }
    }


// =====================================================
// ADD EXERCISE
// =====================================================

    @FXML
    public void handleAddExercise() {

        if (currentPlan == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No plan",
                    "Create or select a training plan first."
            );

            return;
        }


        PlanDay selectedDay =
                daySelector.getValue();


        if (selectedDay == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Missing day",
                    "Please select a day."
            );

            return;
        }


        Exercise selectedExercise =
                exerciseBox.getValue();


        if (selectedExercise == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Missing exercise",
                    "Please select an exercise."
            );

            return;
        }


        // =================================================
        // DURATION
        // =================================================

        double duration;

        String durationText =
                durationField.getText().trim();


        if (durationText.isEmpty()) {

            duration =
                    selectedExercise.getDuration();

        } else {

            try {

                duration =
                        Double.parseDouble(
                                durationText
                        );

            } catch (NumberFormatException e) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Invalid duration",
                        "Duration must be a number."
                );

                return;
            }
        }


        if (duration <= 0) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Invalid duration",
                    "Duration must be greater than 0."
            );

            return;
        }


        // =================================================
        // SETS
        // =================================================

        int sets = 0;

        String setsText =
                setsField.getText().trim();


        if (!setsText.isEmpty()) {

            try {

                sets =
                        Integer.parseInt(
                                setsText
                        );

            } catch (NumberFormatException e) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Invalid sets",
                        "Sets must be a whole number."
                );

                return;
            }


            if (sets < 0) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Invalid sets",
                        "Sets cannot be negative."
                );

                return;
            }
        }


        // =================================================
        // REPS
        // =================================================

        int reps = 0;

        String repsText =
                repsField.getText().trim();


        if (!repsText.isEmpty()) {

            try {

                reps =
                        Integer.parseInt(
                                repsText
                        );

            } catch (NumberFormatException e) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Invalid reps",
                        "Reps must be a whole number."
                );

                return;
            }


            if (reps < 0) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Invalid reps",
                        "Reps cannot be negative."
                );

                return;
            }
        }


        // =================================================
        // ADD EXERCISE
        // =================================================

        planService.addExerciseToDay(
                currentPlan,
                selectedDay.getDayName(),
                selectedExercise,
                duration,
                sets,
                reps
        );


        // =================================================
        // CALCULATE CALORIES
        // =================================================

        double calories =
                selectedExercise.getCalories()
                        * duration
                        / 60.0;


        // =================================================
        // CONSOLE
        // =================================================

        System.out.println(
                "Exercise added:"
        );

        System.out.println(
                "Name: "
                        + selectedExercise.getName()
        );

        System.out.println(
                "Plan: "
                        + currentPlan.getName()
        );

        System.out.println(
                "Day: "
                        + selectedDay.getDayName()
        );

        System.out.println(
                "Duration: "
                        + duration
                        + " min"
        );

        System.out.println(
                "Sets: "
                        + sets
        );

        System.out.println(
                "Reps: "
                        + reps
        );

        System.out.println(
                "Calories: "
                        + calories
        );


        // =================================================
        // UPDATE
        // =================================================

        updateOverview();


        // =================================================
        // RESET INPUT
        // =================================================

        durationField.clear();
        setsField.clear();
        repsField.clear();


        // =================================================
        // RESULT
        // =================================================

        showAlert(
                Alert.AlertType.INFORMATION,
                "Exercise added",
                selectedExercise.getName()
                        + " was added to "
                        + selectedDay.getDayName()
                        + ".\n\n"
                        + "Duration: "
                        + duration
                        + " min\n"
                        + "Sets: "
                        + (sets > 0 ? sets : "-")
                        + "\n"
                        + "Reps: "
                        + (reps > 0 ? reps : "-")
                        + "\n"
                        + "Calories: "
                        + String.format(
                        "%.1f",
                        calories
                )
                        + " kcal"
        );
    }


// =====================================================
// UPDATE OVERVIEW
// =====================================================

    private void updateOverview() {

        if (overviewPlanName == null
                || overviewDates == null
                || overviewContainer == null
                || overviewDuration == null
                || overviewCalories == null) {

            return;
        }


        // ---------------------------------------------
        // No current plan
        // ---------------------------------------------

        if (currentPlan == null) {

            overviewPlanName.setText(
                    "No plan created"
            );

            overviewDates.setText(
                    "Start: - | End: -"
            );

            overviewContainer.getChildren()
                    .clear();

            overviewDuration.setText(
                    "Total duration: 0 min"
            );

            overviewCalories.setText(
                    "Total calories: 0 kcal"
            );

            return;
        }


        // ---------------------------------------------
        // Plan information
        // ---------------------------------------------

        overviewPlanName.setText(
                currentPlan.getName()
        );


        overviewDates.setText(
                "Start: "
                        + currentPlan.getStartDate()
                        + " | End: "
                        + currentPlan.getEndDate()
        );


        // ---------------------------------------------
        // Clear overview
        // ---------------------------------------------

        overviewContainer.getChildren()
                .clear();


        // ---------------------------------------------
        // Days
        // ---------------------------------------------

        for (PlanDay day :
                currentPlan.getDays()) {

            VBox dayBox =
                    new VBox(5);


            dayBox.setStyle(
                    "-fx-background-color: rgba(240,240,240,0.9);"
                            + "-fx-background-radius: 8;"
                            + "-fx-padding: 10;"
            );


            // -----------------------------------------
            // Day name
            // -----------------------------------------

            Label dayLabel =
                    new Label(
                            day.getDayName()
                    );


            dayLabel.setStyle(
                    "-fx-font-weight: bold;"
                            + "-fx-font-size: 15px;"
            );


            dayBox.getChildren()
                    .add(dayLabel);


            // -----------------------------------------
            // No exercises
            // -----------------------------------------

            if (day.isEmpty()) {

                Label emptyLabel =
                        new Label(
                                "No exercises"
                        );


                emptyLabel.setStyle(
                        "-fx-text-fill: gray;"
                );


                dayBox.getChildren()
                        .add(emptyLabel);
            }


            // -----------------------------------------
            // Exercises
            // -----------------------------------------

            else {

                for (PlanExercise planExercise :
                        day.getExercises()) {

                    Exercise exercise =
                            planExercise.getExercise();


                    VBox exerciseBox =
                            new VBox(2);


                    exerciseBox.setStyle(
                            "-fx-padding: 5 0 5 10;"
                    );


                    // Exercise name

                    Label nameLabel =
                            new Label(
                                    exercise.getName()
                            );


                    nameLabel.setStyle(
                            "-fx-font-weight: bold;"
                    );


                    exerciseBox.getChildren()
                            .add(nameLabel);


                    // Duration

                    Label durationLabel =
                            new Label(
                                    "Duration: "
                                            + String.format(
                                            "%.1f",
                                            planExercise.getDuration()
                                    )
                                            + " min"
                            );


                    exerciseBox.getChildren()
                            .add(durationLabel);


                    // Sets

                    if (planExercise.getSets() > 0) {

                        Label setsLabel =
                                new Label(
                                        "Sets: "
                                                + planExercise.getSets()
                                );


                        exerciseBox.getChildren()
                                .add(setsLabel);
                    }


                    // Reps

                    if (planExercise.getReps() > 0) {

                        Label repsLabel =
                                new Label(
                                        "Reps: "
                                                + planExercise.getReps()
                                );


                        exerciseBox.getChildren()
                                .add(repsLabel);
                    }


                    // Calories

                    Label caloriesLabel =
                            new Label(
                                    "Calories: "
                                            + String.format(
                                            "%.1f",
                                            planExercise.getCalories()
                                    )
                                            + " kcal"
                            );


                    exerciseBox.getChildren()
                            .add(caloriesLabel);


                    dayBox.getChildren()
                            .add(exerciseBox);
                }
            }


            // -----------------------------------------
            // Day totals
            // -----------------------------------------

            if (!day.isEmpty()) {

                Label dayTotalLabel =
                        new Label(
                                "Day total: "
                                        + String.format(
                                        "%.1f",
                                        day.getTotalDuration()
                                )
                                        + " min | "
                                        + String.format(
                                        "%.1f",
                                        day.getTotalCalories()
                                )
                                        + " kcal"
                        );


                dayTotalLabel.setStyle(
                        "-fx-font-weight: bold;"
                                + "-fx-padding: 5 0 0 0;"
                );


                dayBox.getChildren()
                        .add(dayTotalLabel);
            }


            // -----------------------------------------
            // Add day
            // -----------------------------------------

            overviewContainer.getChildren()
                    .add(dayBox);
        }


        // ---------------------------------------------
        // Plan totals
        // ---------------------------------------------

        overviewDuration.setText(
                "Total duration: "
                        + String.format(
                        "%.1f",
                        currentPlan.getTotalDuration()
                )
                        + " min"
        );


        overviewCalories.setText(
                "Total calories: "
                        + String.format(
                        "%.1f",
                        currentPlan.getTotalCalories()
                )
                        + " kcal"
        );
    }


// =====================================================
// TEXT OVERVIEW
// =====================================================

    public String getPlanOverview() {

        if (currentPlan == null) {

            return "No training plan created.";
        }


        StringBuilder overview =
                new StringBuilder();


        overview.append(
                "TRAINING PLAN\n"
        );


        overview.append(
                "==============================\n"
        );


        overview.append(
                "Name: "
                        + currentPlan.getName()
                        + "\n"
        );


        overview.append(
                "Start: "
                        + currentPlan.getStartDate()
                        + "\n"
        );


        overview.append(
                "End: "
                        + currentPlan.getEndDate()
                        + "\n\n"
        );


        // ---------------------------------------------
        // Days
        // ---------------------------------------------

        for (PlanDay day :
                currentPlan.getDays()) {

            overview.append(
                    day.getDayName()
            );


            overview.append(
                    "\n------------------------------\n"
            );


            if (day.isEmpty()) {

                overview.append(
                        "No exercises\n\n"
                );

                continue;
            }


            // -----------------------------------------
            // Exercises
            // -----------------------------------------

            for (PlanExercise planExercise :
                    day.getExercises()) {

                Exercise exercise =
                        planExercise.getExercise();


                overview.append(
                        "Exercise: "
                                + exercise.getName()
                                + "\n"
                );


                overview.append(
                        "Duration: "
                                + planExercise.getDuration()
                                + " min\n"
                );


                if (planExercise.getSets() > 0) {

                    overview.append(
                            "Sets: "
                                    + planExercise.getSets()
                                    + "\n"
                    );
                }


                if (planExercise.getReps() > 0) {

                    overview.append(
                            "Reps: "
                                    + planExercise.getReps()
                                    + "\n"
                    );
                }


                overview.append(
                        "Calories: "
                                + String.format(
                                "%.1f",
                                planExercise.getCalories()
                        )
                                + " kcal\n\n"
                );
            }


            // -----------------------------------------
            // Day totals
            // -----------------------------------------

            overview.append(
                    "Day total duration: "
                            + String.format(
                            "%.1f",
                            day.getTotalDuration()
                    )
                            + " min\n"
            );


            overview.append(
                    "Day total calories: "
                            + String.format(
                            "%.1f",
                            day.getTotalCalories()
                    )
                            + " kcal\n\n"
            );
        }


        // ---------------------------------------------
        // Plan totals
        // ---------------------------------------------

        overview.append(
                "==============================\n"
        );


        overview.append(
                "TOTAL PLAN DURATION: "
                        + String.format(
                        "%.1f",
                        currentPlan.getTotalDuration()
                )
                        + " min\n"
        );


        overview.append(
                "TOTAL PLAN CALORIES: "
                        + String.format(
                        "%.1f",
                        currentPlan.getTotalCalories()
                )
                        + " kcal\n"
        );


        return overview.toString();
    }


// =====================================================
// SHOW PLAN OVERVIEW
// =====================================================

    @FXML
    public void handleShowPlanOverview() {

        if (currentPlan == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No plan",
                    "Create or select a training plan first."
            );

            return;
        }


        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );


        alert.setTitle(
                "Training Plan Overview"
        );


        alert.setHeaderText(
                currentPlan.getName()
        );


        alert.setContentText(
                getPlanOverview()
        );


        alert.getDialogPane()
                .setPrefWidth(500);


        alert.getDialogPane()
                .setPrefHeight(600);


        alert.showAndWait();
    }
// =====================================================
// DELETE CURRENT PLAN
// =====================================================

    @FXML
    public void handleDeletePlan() {

        // ---------------------------------------------
        // Prüfen, ob ein Plan ausgewählt ist
        // ---------------------------------------------

        if (currentPlan == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No plan",
                    "Please select a training plan first."
            );

            return;
        }


        // ---------------------------------------------
        // Sicherheitsabfrage
        // ---------------------------------------------

        Alert confirmation =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Delete Training Plan");
        confirmation.setHeaderText(
                "Delete plan \"" + currentPlan.getName() + "\"?"
        );

        confirmation.setContentText(
                "This will delete the selected training plan."
        );


        // ---------------------------------------------
        // Abfrage anzeigen
        // ---------------------------------------------

        var result =
                confirmation.showAndWait();


        if (result.isEmpty()
                || result.get()
                != javafx.scene.control.ButtonType.OK) {

            return;
        }


        // ---------------------------------------------
        // Plan merken
        // ---------------------------------------------

        Plan deletedPlan = currentPlan;


        // ---------------------------------------------
        // Plan aus Liste entfernen
        // ---------------------------------------------

        plans.remove(deletedPlan);


        // ---------------------------------------------
        // Aktuellen Plan zurücksetzen
        // ---------------------------------------------

        currentPlan = null;


        // ---------------------------------------------
        // Plan-Auswahl aktualisieren
        // ---------------------------------------------

        refreshPlanSelector();


        // ---------------------------------------------
        // Auswahl zurücksetzen
        // ---------------------------------------------

        planSelector.getSelectionModel()
                .clearSelection();

        planSelector.setValue(null);


        // ---------------------------------------------
        // Wenn noch Pläne vorhanden sind:
        // ersten Plan auswählen
        // ---------------------------------------------

        if (!plans.isEmpty()) {

            Plan nextPlan =
                    plans.get(0);

            currentPlan = nextPlan;

            planSelector.setValue(nextPlan);

            refreshDays();

        } else {

            // -----------------------------------------
            // Keine Pläne mehr vorhanden
            // -----------------------------------------

            daySelector.setItems(
                    FXCollections.observableArrayList()
            );

            exerciseBox.setItems(
                    FXCollections.observableArrayList()
            );

            exerciseTypeBox.getSelectionModel()
                    .clearSelection();
        }


        // ---------------------------------------------
        // Übersicht aktualisieren
        // ---------------------------------------------

        updateOverview();


        // ---------------------------------------------
        // Meldung
        // ---------------------------------------------

        showAlert(
                Alert.AlertType.INFORMATION,
                "Plan deleted",
                "Training plan \""
                        + deletedPlan.getName()
                        + "\" was deleted."
        );
    }


// =====================================================
// BACK TO MENU
// =====================================================

    @FXML
    public void handleBackToMenu() {

        changeView(
                "mainMenu.fxml"
        );
    }


// =====================================================
// ALERT
// =====================================================

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert =
                new Alert(type);


        alert.setTitle(title);


        alert.setHeaderText(null);


        alert.setContentText(message);


        alert.showAndWait();
    }
}