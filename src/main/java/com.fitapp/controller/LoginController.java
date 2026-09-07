package com.fitapp.controller;
import com.fitapp.navigation.Navigator;

import com.fitapp.model.EmptyFieldException;
import com.fitapp.model.InvalidCredentialsException;
import com.fitapp.model.Session;

import com.fitapp.model.UserDatabaseSQLite;
import com.fitapp.model.UserRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class LoginController implements Controller {

    private Navigator navigator;
    private UserRepository userDB = new UserDatabaseSQLite();

    @FXML private VBox loginPanel;
    @FXML private VBox registerPanel;

    // Login Felder
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    // Register Felder
    @FXML private TextField regUsernameField;
    @FXML private PasswordField regPasswordField;
    @FXML private PasswordField regConfirmField;
    @FXML private Label regErrorLabel;

    // default constructor for FXML loading
    public LoginController(){

    }

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    public void changeView(String fxmlFile) {
        navigator.changeView(fxmlFile);
    }

    // ── Panel wechseln ──────────────────────────────
    @FXML
    public void showRegister() {
        loginPanel.setVisible(false);
        registerPanel.setVisible(true);
    }

    @FXML
    public void showLogin() {
        loginPanel.setVisible(true);
        registerPanel.setVisible(false);
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        try {
            userDB.validateInput(usernameField.getText(), passwordField.getText());
            errorLabel.setVisible(false);

            // Christian: Name vom Login merken. Step Counter und Statistik brauchen ihn.
            Session.setUser(usernameField.getText());

            changeView("mainMenu.fxml");

        } catch (EmptyFieldException e) {
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);

        } catch (InvalidCredentialsException e) {
            errorLabel.setText("Login Failed");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    public void handleRegister() {
        String username = regUsernameField.getText();
        String password = regPasswordField.getText();
        String confirm  = regConfirmField.getText();

        if (username.isBlank() || password.isBlank()) {
            regErrorLabel.setText("Bitte alle Felder ausfüllen!");
            regErrorLabel.setVisible(true);
            return;
        }
        if (!password.equals(confirm)) {
            regErrorLabel.setText("Passwörter stimmen nicht überein!");
            regErrorLabel.setVisible(true);
            return;
        }

        ((UserDatabaseSQLite) userDB).addUser(username, password);
        showLogin();
    }

}