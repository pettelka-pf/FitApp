package com.fitapp.controller;

import com.fitapp.model.*;
import com.fitapp.navigation.Navigator;
import com.fitapp.util.BackgroundImageHelper;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.collections.ObservableList;
import java.util.Optional;

import java.util.Date;

public class ExerciseController implements Controller {

    private Navigator navigator;

// -------------------------
// SERVICE
// -------------------------

    private final ExerciseService exerciseService =
            new ExerciseService();


// -------------------------
// GENERAL FIELDS
// -------------------------

    @FXML
    private StackPane rootPane;

    @FXML
    private ImageView backgroundImage;

    @FXML
    private TextField exerciseNameField;

    @FXML
    private ComboBox<String> exerciseCategory;

    @FXML
    private TextField exerciseCaloriesField;

    @FXML
    private TextField exerciseDurationField;

    @FXML
    private TextField exerciseDifficultyField;


// -------------------------
// WEIGHT FIELDS
// -------------------------

    @FXML
    private VBox weightFields;

    @FXML
    private TextField exerciseWeightField;

    @FXML
    private TextField exerciseRepetitionField;

    @FXML
    private TextField exerciseMuscleGroupField;


// -------------------------
// RUNNING FIELDS
// -------------------------

    @FXML
    private VBox runningFields;

    @FXML
    private TextField exerciseDistanceField;

    @FXML
    private TextField exerciseSpeedField;

    @FXML
    private TextField exerciseStepsField;


// -------------------------
// CALISTHENICS FIELDS
// -------------------------

    @FXML
    private VBox calisthenicsFields;

    @FXML
    private TextField exerciseIntervalField;

    @FXML
    private TextField exerciseExercisesPerRoundField;

    @FXML
    private TextField exerciseRoundsField;


// -------------------------
// SAVED EXERCISES
// -------------------------

    @FXML
    private ListView<Exercise> exerciseListView;

    @FXML
    private VBox exerciseDetails;

    @FXML
    private Label selectedExerciseName;

    @FXML
    private Label selectedExerciseCategory;

    @FXML
    private Label selectedExerciseDescription;

    @FXML
    private Label selectedExerciseDifficulty;

    @FXML
    private Label selectedExerciseDuration;

    @FXML
    private Label selectedExerciseCalories;

    @FXML
    private Label selectedExerciseSpecific1;

    @FXML
    private Label selectedExerciseSpecific2;

    @FXML
    private Label selectedExerciseSpecific3;


// -------------------------
// NAVIGATION
// -------------------------

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

        BackgroundImageHelper.setup(
                rootPane,
                backgroundImage
        );


        exerciseCategory.setItems(
                FXCollections.observableArrayList(
                        "WEIGHT",
                        "CARDIO_RUNNING",
                        "CARDIO_CALISTHENICS"
                )
        );


        exerciseCategory.setOnAction(
                event -> updateFields()
        );


        updateFields();
        setupExerciseList();



        // Details zunächst ausblenden.
        exerciseDetails.setVisible(false);
        exerciseDetails.setManaged(false);


        // Gespeicherte Übungen laden.
        loadExercises();


        /*
         * Wenn eine Übung in der Liste angeklickt wird,
         * werden ihre Details angezeigt.
         */
        exerciseListView
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                showExerciseDetails(newValue)
                );
    }


// -------------------------
// LOAD EXERCISES
// -------------------------

    /**
     * Lädt alle vom aktuell angemeldeten Benutzer
     * gespeicherten Übungen aus der Datenbank.
     */
    private void loadExercises() {

        if (!Session.isLoggedIn()) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Not logged in",
                    "Please log in first."
            );

            return;
        }


        int userId = Session.getUserId();


        Task<java.util.List<Exercise>> task =
                new Task<>() {

                    @Override
                    protected java.util.List<Exercise> call()
                            throws Exception {

                        return exerciseService
                                .getAllExercises(userId);
                    }
                };


        task.setOnSucceeded(event -> {

            ObservableList<Exercise> exercises =
                    FXCollections.observableArrayList(
                            task.getValue()
                    );

            exerciseListView.setItems(exercises);
        });


        task.setOnFailed(event -> {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Database error",
                    "The exercises could not be loaded."
            );
        });


        Thread thread = new Thread(task);

        thread.setDaemon(true);

        thread.start();
    }


// -------------------------
// LIST DISPLAY
// -------------------------

    /**
     * Legt fest, wie die Übungen in der ListView
     * dargestellt werden.
     */
    private void setupExerciseList() {

        exerciseListView.setCellFactory(
                listView -> new javafx.scene.control.ListCell<>() {

                    @Override
                    protected void updateItem(
                            Exercise exercise,
                            boolean empty) {

                        super.updateItem(
                                exercise,
                                empty
                        );


                        if (empty || exercise == null) {

                            setText(null);

                        } else {

                            setText(
                                    exercise.getName()
                                            + "  ("
                                            + getExerciseType(exercise)
                                            + ")"
                            );
                        }
                    }
                }
        );
    }


    /**
     * Gibt den Typ einer Übung zurück.
     */
    private String getExerciseType(Exercise exercise) {

        if (exercise instanceof WeightExercise) {
            return "WEIGHT";
        }

        if (exercise instanceof CardioRunningExercise) {
            return "CARDIO RUNNING";
        }

        if (exercise instanceof CardioCalisthenicsExercise) {
            return "CARDIO CALISTHENICS";
        }

        return "UNKNOWN";
    }


// -------------------------
// SHOW DETAILS
// -------------------------

    /**
     * Zeigt alle gespeicherten Informationen
     * der ausgewählten Übung an.
     */
    private void showExerciseDetails(Exercise exercise) {

        if (exercise == null) {

            exerciseDetails.setVisible(false);
            exerciseDetails.setManaged(false);

            return;
        }


        exerciseDetails.setVisible(true);
        exerciseDetails.setManaged(true);


        selectedExerciseName.setText(
                "Name: " + exercise.getName()
        );


        selectedExerciseCategory.setText(
                "Category: " + getExerciseType(exercise)
        );


        selectedExerciseDescription.setText(
                "Description: "
                        + safeText(exercise.getDescription())
        );


        selectedExerciseDifficulty.setText(
                "Difficulty: "
                        + safeText(exercise.getDifficulty())
        );


        selectedExerciseDuration.setText(
                "Duration: "
                        + exercise.getDuration()
                        + " min"
        );


        selectedExerciseCalories.setText(
                "Calories: "
                        + exercise.getCalories()
                        + " kcal/h"
        );


        // Zunächst alle typspezifischen Felder ausblenden.
        selectedExerciseSpecific1.setVisible(false);
        selectedExerciseSpecific2.setVisible(false);
        selectedExerciseSpecific3.setVisible(false);


        // -------------------------
        // WEIGHT
        // -------------------------

        if (exercise instanceof WeightExercise weight) {

            selectedExerciseSpecific1.setText(
                    "Weight: "
                            + weight.getWeight()
                            + " kg"
            );

            selectedExerciseSpecific2.setText(
                    "Repetitions: "
                            + weight.getRepetition()
            );

            selectedExerciseSpecific3.setText(
                    "Muscle group: "
                            + weight.getMuscleGroup()
            );


            showSpecificLabels();
        }


        // -------------------------
        // RUNNING
        // -------------------------

        else if (exercise instanceof CardioRunningExercise running) {

            selectedExerciseSpecific1.setText(
                    "Distance: "
                            + running.getDistance()
                            + " km"
            );

            selectedExerciseSpecific2.setText(
                    "Speed: "
                            + running.getSpeed()
                            + " km/h"
            );

            selectedExerciseSpecific3.setText(
                    "Steps: "
                            + running.getSteps()
            );


            showSpecificLabels();
        }


        // -------------------------
        // CALISTHENICS
        // -------------------------

        else if (exercise instanceof CardioCalisthenicsExercise calisthenics) {

            selectedExerciseSpecific1.setText(
                    "Interval: "
                            + calisthenics.getInterval()
                            + " min"
            );

            selectedExerciseSpecific2.setText(
                    "Exercises per round: "
                            + calisthenics.getNumOfExercisesPerRound()
            );

            selectedExerciseSpecific3.setText(
                    "Rounds: "
                            + calisthenics.getRound()
            );


            showSpecificLabels();
        }
    }


    private void showSpecificLabels() {

        selectedExerciseSpecific1.setVisible(true);
        selectedExerciseSpecific2.setVisible(true);
        selectedExerciseSpecific3.setVisible(true);
    }


    private String safeText(String text) {

        if (text == null || text.isBlank()) {
            return "-";
        }

        return text;
    }


// -------------------------
// DELETE SELECTED EXERCISE
// -------------------------

    @FXML
    public void handleDeleteExercise() {

        if (!Session.isLoggedIn()) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Not logged in",
                    "Please log in first."
            );

            return;
        }


        Exercise selectedExercise =
                exerciseListView
                        .getSelectionModel()
                        .getSelectedItem();


        if (selectedExercise == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No exercise selected",
                    "Please select an exercise first."
            );

            return;
        }


        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );


        confirmation.setTitle(
                "Delete exercise"
        );

        confirmation.setHeaderText(
                "Delete selected exercise?"
        );

        confirmation.setContentText(
                "Do you really want to delete \""
                        + selectedExercise.getName()
                        + "\"?"
        );


        Optional<ButtonType> result =
                confirmation.showAndWait();


        if (result.isEmpty()
                || result.get() != ButtonType.OK) {

            return;
        }


        int userId =
                Session.getUserId();

        int exerciseId =
                selectedExercise.getId();


        Task<Void> task =
                new Task<>() {

                    @Override
                    protected Void call()
                            throws Exception {

                        exerciseService.deleteExercise(
                                userId,
                                exerciseId
                        );

                        return null;
                    }
                };


        task.setOnSucceeded(event -> {

            exerciseListView
                    .getItems()
                    .remove(selectedExercise);


            exerciseListView
                    .getSelectionModel()
                    .clearSelection();


            exerciseDetails.setVisible(false);
            exerciseDetails.setManaged(false);


            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Exercise deleted",
                    "The exercise was deleted successfully."
            );
        });


        task.setOnFailed(event -> {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Database error",
                    "The exercise could not be deleted."
            );
        });


        Thread thread = new Thread(task);

        thread.setDaemon(true);

        thread.start();
    }


// -------------------------
// SHOW CORRECT FIELDS
// -------------------------

    private void updateFields() {

        String category =
                exerciseCategory.getValue();


        boolean isWeight =
                "WEIGHT".equals(category);


        boolean isRunning =
                "CARDIO_RUNNING".equals(category);


        boolean isCalisthenics =
                "CARDIO_CALISTHENICS".equals(category);


        weightFields.setVisible(isWeight);
        weightFields.setManaged(isWeight);


        runningFields.setVisible(isRunning);
        runningFields.setManaged(isRunning);


        calisthenicsFields.setVisible(isCalisthenics);
        calisthenicsFields.setManaged(isCalisthenics);
    }


// -------------------------
// CREATE EXERCISE
// -------------------------

    @FXML
    public void handleCreateExercise() {

        if (!Session.isLoggedIn()) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Not logged in",
                    "Please log in first."
            );

            return;
        }


        String name =
                exerciseNameField
                        .getText()
                        .trim();


        String category =
                exerciseCategory.getValue();


        // -------------------------
        // BASIC VALIDATION
        // -------------------------

        if (name.isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Missing information",
                    "Please enter an exercise name."
            );

            return;
        }


        if (category == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Missing information",
                    "Please select an exercise category."
            );

            return;
        }


        // -------------------------
        // GENERAL VALUES
        // -------------------------

        double calories;
        double duration;


        try {

            calories =
                    Double.parseDouble(
                            exerciseCaloriesField
                                    .getText()
                                    .trim()
                    );


            duration =
                    Double.parseDouble(
                            exerciseDurationField
                                    .getText()
                                    .trim()
                    );

        } catch (NumberFormatException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Invalid input",
                    "Calories and duration must be numbers."
            );

            return;
        }


        if (calories <= 0
                || duration <= 0) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Invalid input",
                    "Calories and duration must be greater than 0."
            );

            return;
        }


        // -------------------------
        // DIFFICULTY
        // -------------------------

        String difficulty =
                exerciseDifficultyField
                        .getText()
                        .trim();


        if (difficulty.isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Missing information",
                    "Please enter the difficulty."
            );

            return;
        }


        // -------------------------
        // CREATE EXERCISE
        // -------------------------

        Exercise exercise;


        // =====================================================
        // WEIGHT
        // =====================================================

        if ("WEIGHT".equals(category)) {

            try {

                double weight =
                        Double.parseDouble(
                                exerciseWeightField
                                        .getText()
                                        .trim()
                        );


                int repetition =
                        Integer.parseInt(
                                exerciseRepetitionField
                                        .getText()
                                        .trim()
                        );


                String muscleGroup =
                        exerciseMuscleGroupField
                                .getText()
                                .trim();


                if (weight <= 0
                        || repetition <= 0
                        || muscleGroup.isEmpty()) {

                    showAlert(
                            Alert.AlertType.WARNING,
                            "Invalid input",
                            "Please enter valid weight, repetitions and muscle group."
                    );

                    return;
                }


                exercise =
                        new WeightExercise(
                                0,
                                name,
                                "",
                                new Date(),
                                difficulty,
                                duration,
                                calories,
                                weight,
                                repetition,
                                muscleGroup
                        );


            } catch (NumberFormatException e) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Invalid input",
                        "Weight must be a number and repetitions must be an integer."
                );

                return;
            }
        }


        // =====================================================
        // CARDIO RUNNING
        // =====================================================

        else if ("CARDIO_RUNNING".equals(category)) {

            try {

                double distance =
                        Double.parseDouble(
                                exerciseDistanceField
                                        .getText()
                                        .trim()
                        );


                double speed =
                        Double.parseDouble(
                                exerciseSpeedField
                                        .getText()
                                        .trim()
                        );


                int steps =
                        Integer.parseInt(
                                exerciseStepsField
                                        .getText()
                                        .trim()
                        );


                if (distance <= 0
                        || speed <= 0
                        || steps < 0) {

                    showAlert(
                            Alert.AlertType.WARNING,
                            "Invalid input",
                            "Please enter valid distance, speed and steps."
                    );

                    return;
                }


                exercise =
                        new CardioRunningExercise(
                                0,
                                name,
                                "",
                                new Date(),
                                difficulty,
                                duration,
                                calories,
                                distance,
                                speed,
                                steps
                        );


            } catch (NumberFormatException e) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Invalid input",
                        "Distance and speed must be numbers and steps must be an integer."
                );

                return;
            }
        }


        // =====================================================
        // CARDIO CALISTHENICS
        // =====================================================

        else {

            try {

                double interval =
                        Double.parseDouble(
                                exerciseIntervalField
                                        .getText()
                                        .trim()
                        );


                int exercisesPerRound =
                        Integer.parseInt(
                                exerciseExercisesPerRoundField
                                        .getText()
                                        .trim()
                        );


                int rounds =
                        Integer.parseInt(
                                exerciseRoundsField
                                        .getText()
                                        .trim()
                        );


                if (interval <= 0
                        || exercisesPerRound <= 0
                        || rounds <= 0) {

                    showAlert(
                            Alert.AlertType.WARNING,
                            "Invalid input",
                            "Please enter valid interval, exercises per round and rounds."
                    );

                    return;
                }


                exercise =
                        new CardioCalisthenicsExercise(
                                0,
                                name,
                                "",
                                new Date(),
                                difficulty,
                                duration,
                                calories,
                                interval,
                                exercisesPerRound,
                                rounds
                        );


            } catch (NumberFormatException e) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Invalid input",
                        "Interval must be a number and exercises/rounds must be integers."
                );

                return;
            }
        }


        // =====================================================
        // SAVE EXERCISE
        // =====================================================

        Exercise toSave = exercise;

        int userId =
                Session.getUserId();


        Task<Exercise> task =
                new Task<>() {

                    @Override
                    protected Exercise call()
                            throws Exception {

                        return exerciseService
                                .addExercise(
                                        userId,
                                        toSave
                                );
                    }
                };


        task.setOnSucceeded(event -> {

            Exercise saved =
                    task.getValue();


            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Exercise created",
                    "Exercise \""
                            + saved.getName()
                            + "\" was saved."
            );


            clearFields();


            /*
             * Nach dem Speichern die Liste
             * direkt neu laden.
             */
            loadExercises();
        });


        task.setOnFailed(event ->
                showAlert(
                        Alert.AlertType.ERROR,
                        "Database error",
                        "The exercise could not be saved."
                )
        );


        Thread thread =
                new Thread(task);

        thread.setDaemon(true);

        thread.start();
    }


// -------------------------
// CLEAR FIELDS
// -------------------------

    private void clearFields() {

        exerciseNameField.clear();

        exerciseCategory
                .getSelectionModel()
                .clearSelection();

        exerciseCaloriesField.clear();

        exerciseDurationField.clear();

        exerciseDifficultyField.clear();


        // Weight

        exerciseWeightField.clear();

        exerciseRepetitionField.clear();

        exerciseMuscleGroupField.clear();


        // Running

        exerciseDistanceField.clear();

        exerciseSpeedField.clear();

        exerciseStepsField.clear();


        // Calisthenics

        exerciseIntervalField.clear();

        exerciseExercisesPerRoundField.clear();

        exerciseRoundsField.clear();


        updateFields();
    }


// -------------------------
// BACK TO MENU
// -------------------------

    @FXML
    public void handleBackToMenu() {

        changeView("mainMenu.fxml");
    }


// -------------------------
// ALERT
// -------------------------

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