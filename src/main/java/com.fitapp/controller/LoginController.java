package com.fitapp.controller;

import com.fitapp.model.*;
import com.fitapp.navigation.Navigator;
import com.fitapp.util.BackgroundImageHelper;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;


public class LoginController implements Controller {

    private Navigator navigator;

    private UserRepository userDB = new UserDatabaseSQLite();

    // -------------------------
    // MAIN WINDOW
    // -------------------------

    @FXML
    private StackPane rootPane;

    @FXML
    private ImageView backgroundImage;


    // -------------------------
    // LOGIN PANEL
    // -------------------------

    @FXML
    private VBox loginPanel;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;


    // -------------------------
    // REGISTER PANEL
    // -------------------------

    @FXML
    private VBox registerPanel;

    @FXML
    private TextField regUsernameField;

    @FXML
    private PasswordField regPasswordField;

    @FXML
    private PasswordField regConfirmField;

    @FXML
    private Label regErrorLabel;


    // -------------------------
    // CONSTRUCTOR
    // -------------------------

    // Default constructor for FXML loading
    public LoginController() {
    }


    // -------------------------
    // INITIALIZE
    // -------------------------

    @FXML
    public void initialize() {

        // Hintergrundbild automatisch an die
        // Größe des Fensters anpassen.
        BackgroundImageHelper.setup(
                rootPane,
                backgroundImage
        );
    }


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
    // PANEL WECHSELN
    // -------------------------

    @FXML
    public void showRegister() {

        loginPanel.setVisible(false);
        registerPanel.setVisible(true);

        // Eventuelle vorherige Fehlermeldung ausblenden
        errorLabel.setVisible(false);
        regErrorLabel.setVisible(false);
    }


    @FXML
    public void showLogin() {

        loginPanel.setVisible(true);
        registerPanel.setVisible(false);

        // Eventuelle vorherige Fehlermeldung ausblenden
        errorLabel.setVisible(false);
        regErrorLabel.setVisible(false);
    }


    // -------------------------
    // LOGIN
    // -------------------------

    @FXML
    public void handleLogin(ActionEvent event) {

        try {

            String username = usernameField.getText();

            userDB.validateInput(
                    username,
                    passwordField.getText()
            );

            Session.login(
                    ((UserDatabaseSQLite) userDB)
                            .findIdByUsername(username),
                    username
            );

            errorLabel.setVisible(false);

            changeView("mainMenu.fxml");

        } catch (EmptyFieldException e) {

            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);

        } catch (InvalidCredentialsException e) {

            errorLabel.setText("Login Failed");
            errorLabel.setVisible(true);
        }
    }


    // -------------------------
    // REGISTER
    // -------------------------

    @FXML
    public void handleRegister() {

        String username = regUsernameField.getText();
        String password = regPasswordField.getText();
        String confirm = regConfirmField.getText();

        if (username.isBlank() || password.isBlank()) {

            regErrorLabel.setText(
                    "Bitte alle Felder ausfüllen!"
            );

            regErrorLabel.setVisible(true);
            return;
        }

        if (!password.equals(confirm)) {

            regErrorLabel.setText(
                    "Passwörter stimmen nicht überein!"
            );

            regErrorLabel.setVisible(true);
            return;
        }

        if (((UserDatabaseSQLite) userDB)
                .addUser(username, password)) {

            // Registrierung erfolgreich
            showLogin();

        } else {

            regErrorLabel.setText(
                    "Benutzername ist schon vergeben!"
            );

            regErrorLabel.setVisible(true);
        }
    }
}
