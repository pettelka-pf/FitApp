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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlanController implements Controller {

    // =====================================================
    // NAVIGATION
    // =====================================================

    private Navigator navigator;


    @Override
    public void setNavigator(
            Navigator navigator) {

        this.navigator = navigator;
    }


    @Override
    public void changeView(
            String fxmlFile) {

        navigator.changeView(
                fxmlFile
        );
    }


    // =====================================================
    // SERVICES / MODEL
    // =====================================================

    private final PlanService planService =
            new PlanService();


    private final ExerciseService exerciseService =
            new ExerciseService();


    /*
     * Lokale Liste der aktuell geladenen Pläne.
     *
     * Die eigentliche dauerhafte Speicherung erfolgt
     * in der Datenbank.
     */
    private final List<Plan> plans =
            new ArrayList<>();


    /*
     * Aktuell ausgewählter Plan.
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

    @FXML
    private ComboBox<Plan> planSelector;


    // =====================================================
    // INITIALIZE
    // =====================================================

    @FXML
    public void initialize() {

        // -------------------------------------------------
        // Background
        // -------------------------------------------------

        BackgroundImageHelper.setup(
                rootPane,
                backgroundImage
        );


        // -------------------------------------------------
        // Exercise categories
        // -------------------------------------------------

        exerciseTypeBox.setItems(
                FXCollections.observableArrayList(
                        "WEIGHT",
                        "CARDIO_RUNNING",
                        "CARDIO_CALISTHENICS"
                )
        );


        // -------------------------------------------------
        // Weekdays
        // -------------------------------------------------

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


        // -------------------------------------------------
        // Initially empty exercise list
        // -------------------------------------------------

        exerciseBox.setItems(
                FXCollections.observableArrayList()
        );


        // -------------------------------------------------
        // Initially empty day list
        // -------------------------------------------------

        daySelector.setItems(
                FXCollections.observableArrayList()
        );


        // -------------------------------------------------
        // Plan selector
        // -------------------------------------------------

        planSelector.setItems(
                FXCollections.observableArrayList()
        );


        // -------------------------------------------------
        // Plan selector display
        // -------------------------------------------------

        planSelector.setCellFactory(
                listView -> {

                    javafx.scene.control.ListCell<Plan> cell =
                            new javafx.scene.control.ListCell<>() {

                                @Override
                                protected void updateItem(
                                        Plan plan,
                                        boolean empty) {

                                    super.updateItem(
                                            plan,
                                            empty
                                    );


                                    if (empty
                                            || plan == null) {

                                        setText(null);

                                    } else {

                                        setText(
                                                plan.getName()
                                        );
                                    }
                                }
                            };


                    return cell;
                }
        );


        planSelector.setButtonCell(
                new javafx.scene.control.ListCell<Plan>() {

                    @Override
                    protected void updateItem(
                            Plan plan,
                            boolean empty) {

                        super.updateItem(
                                plan,
                                empty
                        );


                        if (empty
                                || plan == null) {

                            setText(
                                    "no plan available"
                            );

                        } else {

                            setText(
                                    plan.getName()
                            );
                        }
                    }
                }
        );


        // -------------------------------------------------
        // Exercise type selection
        // -------------------------------------------------

        exerciseTypeBox.setOnAction(
                event ->
                        handleTypeSelect()
        );


        // -------------------------------------------------
        // Pläne aus Datenbank laden
        // -------------------------------------------------

        loadPlans();


        // -------------------------------------------------
        // Initial overview
        // -------------------------------------------------

        updateOverview();
    }


    // =====================================================
    // LOAD PLANS
    // =====================================================

    private void loadPlans() {

        try {

            plans.clear();


            List<Plan> databasePlans =
                    planService.getPlansForUser(
                            Session.getUserId()
                    );


            plans.addAll(
                    databasePlans
            );


            refreshPlanSelector();


            if (!plans.isEmpty()) {

                currentPlan =
                        plans.get(0);


                planSelector.setValue(
                        currentPlan
                );


                refreshDays();

            } else {

                currentPlan = null;


                daySelector.setItems(
                        FXCollections.observableArrayList()
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();


            showAlert(
                    Alert.AlertType.ERROR,
                    "Database error",
                    "Training plans could not be loaded."
            );
        }
    }


    // =====================================================
    // CREATE PLAN
    // =====================================================

    @FXML
    public void handleCreatePlan() {

        String planName =
                planNameField
                        .getText()
                        .trim();


        // -------------------------------------------------
        // Validation
        // -------------------------------------------------

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
                .isBefore(
                        startDatePicker.getValue()
                )) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Invalid dates",
                    "The end date cannot be before the start date."
            );

            return;
        }


        // -------------------------------------------------
        // Create Plan
        // -------------------------------------------------

        Plan newPlan =
                planService.createPlan(
                        planName,
                        java.sql.Date.valueOf(
                                startDatePicker.getValue()
                        ),
                        java.sql.Date.valueOf(
                                endDatePicker.getValue()
                        )
                );


        // -------------------------------------------------
        // Database speichern
        // -------------------------------------------------

        try {

            planService.savePlan(
                    Session.getUserId(),
                    newPlan
            );

        } catch (SQLException e) {

            e.printStackTrace();


            showAlert(
                    Alert.AlertType.ERROR,
                    "Database error",
                    "The training plan could not be saved."
            );

            return;
        }


        // -------------------------------------------------
        // Local list
        // -------------------------------------------------

        plans.add(
                newPlan
        );


        currentPlan =
                newPlan;


        // -------------------------------------------------
        // Update selector
        // -------------------------------------------------

        refreshPlanSelector();


        planSelector.setValue(
                newPlan
        );


        // -------------------------------------------------
        // Reset
        // -------------------------------------------------

        daySelector.setItems(
                FXCollections.observableArrayList()
        );


        exerciseBox.setItems(
                FXCollections.observableArrayList()
        );


        exerciseTypeBox
                .getSelectionModel()
                .clearSelection();


        // -------------------------------------------------
        // Overview
        // -------------------------------------------------

        updateOverview();


        planNameField.clear();


        showAlert(
                Alert.AlertType.INFORMATION,
                "Plan created",
                "Training plan \""
                        + planName
                        + "\" was created successfully."
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

    @FXML
    public void handlePlanSelection() {

        Plan selectedPlan =
                planSelector.getValue();


        if (selectedPlan == null) {
            return;
        }


        currentPlan =
                selectedPlan;


        refreshDays();


        exerciseBox.setItems(
                FXCollections.observableArrayList()
        );


        exerciseTypeBox
                .getSelectionModel()
                .clearSelection();


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
                    dayName
                            + " has already been added."
            );

            return;
        }


        // -------------------------------------------------
        // Add day in Java
        // -------------------------------------------------

        planService.addDayToPlan(
                currentPlan,
                dayName
        );


        // -------------------------------------------------
        // Datenbank aktualisieren
        // -------------------------------------------------

        if (!saveCurrentPlan()) {
            return;
        }


        // -------------------------------------------------
        // Refresh
        // -------------------------------------------------

        refreshDays();


        for (PlanDay day :
                daySelector.getItems()) {

            if (day.getDayName()
                    .equalsIgnoreCase(
                            dayName
                    )) {

                daySelector.setValue(
                        day
                );

                break;
            }
        }


        updateOverview();


        showAlert(
                Alert.AlertType.INFORMATION,
                "Day added",
                "Day \""
                        + dayName
                        + "\" was added to the plan."
        );
    }


    // =====================================================
    // REFRESH DAYS
    // =====================================================

    private void refreshDays() {

        if (currentPlan == null) {

            daySelector.setItems(
                    FXCollections.observableArrayList()
            );

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


        List<Exercise> allExercises;


        try {

            allExercises =
                    exerciseService.getAllExercises(
                            Session.getUserId()
                    );

        } catch (SQLException e) {

            e.printStackTrace();


            showAlert(
                    Alert.AlertType.ERROR,
                    "Database error",
                    "Exercises could not be loaded."
            );

            return;
        }


        List<Exercise> filteredExercises =
                new ArrayList<>();


        // -------------------------------------------------
        // Filter
        // -------------------------------------------------

        for (Exercise exercise :
                allExercises) {

            if ("WEIGHT".equals(
                    selectedType
            )
                    && exercise
                    instanceof WeightExercise) {

                filteredExercises.add(
                        exercise
                );

            } else if (
                    "CARDIO_RUNNING".equals(
                            selectedType
                    )
                            && exercise
                            instanceof CardioRunningExercise) {

                filteredExercises.add(
                        exercise
                );

            } else if (
                    "CARDIO_CALISTHENICS".equals(
                            selectedType
                    )
                            && exercise
                            instanceof CardioCalisthenicsExercise) {

                filteredExercises.add(
                        exercise
                );
            }
        }


        exerciseBox.setItems(
                FXCollections.observableArrayList(
                        filteredExercises
                )
        );


        exerciseBox
                .getSelectionModel()
                .clearSelection();
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
                durationField
                        .getText()
                        .trim();


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
                setsField
                        .getText()
                        .trim();


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
                repsField
                        .getText()
                        .trim();


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
        // ADD TO PLAN
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
        // SAVE DATABASE
        // =================================================

        if (!saveCurrentPlan()) {
            return;
        }


        // =================================================
        // CALCULATE CALORIES
        // =================================================

        double calories =
                selectedExercise.getCalories()
                        * duration
                        / 60.0;


        // =================================================
        // UPDATE
        // =================================================

        updateOverview();


        // =================================================
        // RESET
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
                        + (sets > 0
                        ? sets
                        : "-")
                        + "\n"
                        + "Reps: "
                        + (reps > 0
                        ? reps
                        : "-")
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
    // SAVE CURRENT PLAN
    // =====================================================

    private boolean saveCurrentPlan() {

        if (currentPlan == null) {
            return false;
        }


        try {

            planService.savePlan(
                    Session.getUserId(),
                    currentPlan
            );


            return true;

        } catch (SQLException e) {

            e.printStackTrace();


            showAlert(
                    Alert.AlertType.ERROR,
                    "Database error",
                    "The training plan could not be saved."
            );


            return false;
        }
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


        // -------------------------------------------------
        // No current plan
        // -------------------------------------------------

        if (currentPlan == null) {

            overviewPlanName.setText(
                    "No plan created"
            );


            overviewDates.setText(
                    "Start: - | End: -"
            );


            overviewContainer
                    .getChildren()
                    .clear();


            overviewDuration.setText(
                    "Total duration: 0 min"
            );


            overviewCalories.setText(
                    "Total calories: 0 kcal"
            );


            return;
        }


        // -------------------------------------------------
        // Plan information
        // -------------------------------------------------

        overviewPlanName.setText(
                currentPlan.getName()
        );


        overviewDates.setText(
                "Start: "
                        + currentPlan.getStartDate()
                        + " | End: "
                        + currentPlan.getEndDate()
        );


        // -------------------------------------------------
        // Clear overview
        // -------------------------------------------------

        overviewContainer
                .getChildren()
                .clear();


        // -------------------------------------------------
        // Days
        // -------------------------------------------------

        for (PlanDay day :
                currentPlan.getDays()) {

            VBox dayBox =
                    new VBox(5);


            dayBox.setStyle(
                    "-fx-background-color: rgba(240,240,240,0.9);"
                            + "-fx-background-radius: 8;"
                            + "-fx-padding: 10;"
            );


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


            // -------------------------------------------------
            // No exercises
            // -------------------------------------------------

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


            // -------------------------------------------------
            // Exercises
            // -------------------------------------------------

            else {

                for (PlanExercise planExercise :
                        day.getExercises()) {

                    Exercise exercise =
                            planExercise
                                    .getExercise();


                    VBox exerciseBox =
                            new VBox(2);


                    exerciseBox.setStyle(
                            "-fx-padding: 5 0 5 10;"
                    );


                    Label nameLabel =
                            new Label(
                                    exercise.getName()
                            );


                    nameLabel.setStyle(
                            "-fx-font-weight: bold;"
                    );


                    exerciseBox.getChildren()
                            .add(nameLabel);


                    Label durationLabel =
                            new Label(
                                    "Duration: "
                                            + String.format(
                                            "%.1f",
                                            planExercise
                                                    .getDuration()
                                    )
                                            + " min"
                            );


                    exerciseBox.getChildren()
                            .add(
                                    durationLabel
                            );


                    if (planExercise.getSets() > 0) {

                        Label setsLabel =
                                new Label(
                                        "Sets: "
                                                + planExercise
                                                .getSets()
                                );


                        exerciseBox.getChildren()
                                .add(
                                        setsLabel
                                );
                    }


                    if (planExercise.getReps() > 0) {

                        Label repsLabel =
                                new Label(
                                        "Reps: "
                                                + planExercise
                                                .getReps()
                                );


                        exerciseBox.getChildren()
                                .add(
                                        repsLabel
                                );
                    }


                    Label caloriesLabel =
                            new Label(
                                    "Calories: "
                                            + String.format(
                                            "%.1f",
                                            planExercise
                                                    .getCalories()
                                    )
                                            + " kcal"
                            );


                    exerciseBox.getChildren()
                            .add(
                                    caloriesLabel
                            );


                    dayBox.getChildren()
                            .add(
                                    exerciseBox
                            );
                }
            }


            // -------------------------------------------------
            // Day totals
            // -------------------------------------------------

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
                        .add(
                                dayTotalLabel
                        );
            }


            overviewContainer
                    .getChildren()
                    .add(
                            dayBox
                    );
        }


        // -------------------------------------------------
        // Plan totals
        // -------------------------------------------------

        overviewDuration.setText(
                "Total duration: "
                        + String.format(
                        "%.1f",
                        currentPlan
                                .getTotalDuration()
                )
                        + " min"
        );


        overviewCalories.setText(
                "Total calories: "
                        + String.format(
                        "%.1f",
                        currentPlan
                                .getTotalCalories()
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


            for (PlanExercise planExercise :
                    day.getExercises()) {

                Exercise exercise =
                        planExercise
                                .getExercise();


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
                                planExercise
                                        .getCalories()
                        )
                                + " kcal\n\n"
                );
            }


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


        overview.append(
                "==============================\n"
        );


        overview.append(
                "TOTAL PLAN DURATION: "
                        + String.format(
                        "%.1f",
                        currentPlan
                                .getTotalDuration()
                )
                        + " min\n"
        );


        overview.append(
                "TOTAL PLAN CALORIES: "
                        + String.format(
                        "%.1f",
                        currentPlan
                                .getTotalCalories()
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

        if (currentPlan == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No plan",
                    "Please select a training plan first."
            );

            return;
        }


        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );


        confirmation.setTitle(
                "Delete Training Plan"
        );


        confirmation.setHeaderText(
                "Delete plan \""
                        + currentPlan.getName()
                        + "\"?"
        );


        confirmation.setContentText(
                "This will delete the selected training plan."
        );


        var result =
                confirmation.showAndWait();


        if (result.isEmpty()
                || result.get()
                != javafx.scene.control.ButtonType.OK) {

            return;
        }


        Plan deletedPlan =
                currentPlan;


        // -------------------------------------------------
        // DATABASE
        // -------------------------------------------------

        try {

            planService.deletePlan(
                    Session.getUserId(),
                    deletedPlan.getId()
            );

        } catch (SQLException e) {

            e.printStackTrace();


            showAlert(
                    Alert.AlertType.ERROR,
                    "Database error",
                    "The training plan could not be deleted."
            );

            return;
        }


        // -------------------------------------------------
        // LOCAL LIST
        // -------------------------------------------------

        plans.remove(
                deletedPlan
        );


        currentPlan = null;


        refreshPlanSelector();


        planSelector
                .getSelectionModel()
                .clearSelection();


        planSelector.setValue(
                null
        );


        // -------------------------------------------------
        // Next plan
        // -------------------------------------------------

        if (!plans.isEmpty()) {

            Plan nextPlan =
                    plans.get(0);


            currentPlan =
                    nextPlan;


            planSelector.setValue(
                    nextPlan
            );


            refreshDays();

        } else {

            daySelector.setItems(
                    FXCollections.observableArrayList()
            );


            exerciseBox.setItems(
                    FXCollections.observableArrayList()
            );


            exerciseTypeBox
                    .getSelectionModel()
                    .clearSelection();
        }


        updateOverview();


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


        alert.setTitle(
                title
        );


        alert.setHeaderText(
                null
        );


        alert.setContentText(
                message
        );


        alert.showAndWait();
    }
}
