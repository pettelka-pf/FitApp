package com.fitapp.controller;

import com.fitapp.model.Session;
import com.fitapp.model.Statistic;
import com.fitapp.navigation.Navigator;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Christian: Seite "StepCounter Statistik".
 * Man waehlt einen Monat. Das Balkendiagramm zeigt pro Tag die
 * erreichten Schritte des angemeldeten Benutzers (aus steps.csv).
 */
public class StatisticsController implements Controller {

    private Navigator navigator;

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    public void changeView(String fxmlFile) {
        navigator.changeView(fxmlFile);
    }

    // Christian: Elemente aus der FXML-Datei.
    @FXML private Label userNameLabel;
    @FXML private ComboBox<String> monatBox;
    @FXML private BarChart<String, Number> chart;

    // Christian: laeuft automatisch nach dem Laden der FXML.
    @FXML
    public void initialize() {

        // Christian: angemeldeten Benutzer anzeigen.
        userNameLabel.setText("Angemeldet: " + Session.getUser());

        // Christian: letzte 12 Monate in die Auswahl, aktueller Monat vorne.
        YearMonth jetzt = YearMonth.now();
        for (int i = 0; i < 12; i++) {
            monatBox.getItems().add(jetzt.minusMonths(i).toString());
        }
        monatBox.setValue(jetzt.toString());

        zeichneDiagramm();
    }

    // Christian: anderer Monat gewaehlt -> Diagramm neu zeichnen.
    @FXML
    public void handleMonatWechsel() {
        zeichneDiagramm();
    }

    // Christian: Balkendiagramm fuellen - ein Balken pro Tag des Monats.
    private void zeichneDiagramm() {

        String monatText = monatBox.getValue();
        if (monatText == null) {
            return;
        }
        YearMonth monat = YearMonth.parse(monatText);

        // Christian: Statistic wertet steps.csv fuer Benutzer und Monat aus.
        Statistic s = new Statistic(
                Session.getUser(),
                monat.atDay(1),
                monat.atDay(monat.lengthOfMonth()));

        // Christian: eine Datenreihe - Tag -> erreichte Schritte.
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Erreichte Schritte");

        for (int tagNr = 1; tagNr <= monat.lengthOfMonth(); tagNr++) {
            LocalDate tag = monat.atDay(tagNr);
            int schritte = s.schritteAmTag(tag);
            serie.getData().add(new XYChart.Data<>(String.valueOf(tagNr), schritte));
        }

        // Christian: altes Diagramm loeschen, neues setzen.
        chart.getData().clear();
        chart.getData().add(serie);
    }

    // Christian: zurueck ins Hauptmenue.
    @FXML
    public void handleBackToMenu() {
        changeView("mainMenu.fxml");
    }
}
