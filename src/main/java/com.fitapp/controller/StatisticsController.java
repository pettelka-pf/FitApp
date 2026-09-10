package com.fitapp.controller;

import com.fitapp.model.Session;
import com.fitapp.model.Statistic;
import com.fitapp.navigation.Navigator;
import com.fitapp.util.BackgroundImageHelper;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Christian: Seite "StepCounter Statistik".
 *
 * Man waehlt einen Monat. Das Balkendiagramm zeigt pro Tag
 * entweder die erreichten Schritte oder die aufgenommenen
 * Kalorien des angemeldeten Benutzers.
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

    // Elemente aus der FXML-Datei.
    @FXML private Label userNameLabel;
    @FXML private ComboBox<String> monatBox;
    @FXML private BarChart<String, Number> chart;
    @FXML private Button statistikWechselButton;
    @FXML private NumberAxis yAxis;

    // Background
    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundImage;

    // false = Schritte, true = Kalorien
    private boolean zeigeKalorien = false;

    // Laeuft automatisch nach dem Laden der FXML.
    @FXML
    public void initialize() {

        // Hintergrundbild automatisch an die Fenstergröße anpassen.
        BackgroundImageHelper.setup(
                rootPane,
                backgroundImage
        );

        // Angemeldeten Benutzer anzeigen.
        userNameLabel.setText(
                "Angemeldet: " + Session.getUsername()
        );

        // Letzte 12 Monate in die Auswahl,
        // aktueller Monat vorne.
        YearMonth jetzt = YearMonth.now();

        for (int i = 0; i < 12; i++) {
            monatBox.getItems().add(
                    jetzt.minusMonths(i).toString()
            );
        }

        monatBox.setValue(jetzt.toString());

        // Standardmaessig Schritte anzeigen.
        yAxis.setLabel("Erreichte Schritte");
        statistikWechselButton.setText("Kalorien anzeigen");

        zeichneDiagramm();
    }

    // Anderer Monat gewaehlt -> Diagramm neu zeichnen.
    @FXML
    public void handleMonatWechsel() {
        zeichneDiagramm();
    }

    // Zwischen Schritte und Kalorien wechseln.
    @FXML
    public void handleStatistikWechsel() {

        zeigeKalorien = !zeigeKalorien;

        if (zeigeKalorien) {

            statistikWechselButton.setText(
                    "Schritte anzeigen"
            );

            yAxis.setLabel(
                    "Aufgenommene Kalorien (kcal)"
            );

        } else {

            statistikWechselButton.setText(
                    "Kalorien anzeigen"
            );

            yAxis.setLabel(
                    "Erreichte Schritte"
            );
        }

        zeichneDiagramm();
    }

    // Balkendiagramm fuellen - ein Balken pro Tag des Monats.
    private void zeichneDiagramm() {

        String monatText = monatBox.getValue();

        if (monatText == null) {
            return;
        }

        YearMonth monat = YearMonth.parse(monatText);

        // Ohne Login gibt es keine Daten.
        if (!Session.isLoggedIn()) {
            chart.getData().clear();
            return;
        }

        Statistic s = new Statistic(
                Session.getUserId(),
                monat.atDay(1),
                monat.atDay(monat.lengthOfMonth())
        );

        XYChart.Series<String, Number> serie =
                new XYChart.Series<>();

        if (zeigeKalorien) {
            serie.setName("Aufgenommene Kalorien");
        } else {
            serie.setName("Erreichte Schritte");
        }

        for (int tagNr = 1;
             tagNr <= monat.lengthOfMonth();
             tagNr++) {

            LocalDate tag = monat.atDay(tagNr);

            Number wert;

            if (zeigeKalorien) {

                wert = s.kalorienAmTag(tag);

            } else {

                wert = s.schritteAmTag(tag);
            }

            serie.getData().add(
                    new XYChart.Data<>(
                            String.valueOf(tagNr),
                            wert
                    )
            );
        }

        // Altes Diagramm loeschen, neues setzen.
        chart.getData().clear();
        chart.getData().add(serie);
    }

    // Zurueck ins Hauptmenue.
    @FXML
    public void handleBackToMenu() {
        changeView("mainMenu.fxml");
    }
}
