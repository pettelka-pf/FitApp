package com.fitapp.controller;

import com.fitapp.model.Session;
import com.fitapp.model.StepCsv;
import com.fitapp.model.StepEntry;
import com.fitapp.navigation.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Christian: Step-Counter-Seite mit drei Spalten.
 * LINKS  - Rechner: aus Gewicht und Zeitraum das noetige Tagesziel (Soll).
 * MITTE  - Schritte fuer einen Tag eintragen (Ist).
 * RECHTS - Monatsuebersicht: Soll gegen Ist, Monat waehlbar.
 */
public class StepCounterController implements Controller {

    private Navigator navigator;

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    public void changeView(String fxmlFile) {
        navigator.changeView(fxmlFile);
    }

    // Christian: zuletzt berechnetes Schrittziel. 0 = noch nicht berechnet.
    private long sollSchritte = 0;

    // Christian: Felder der linken Spalte (Rechner).
    @FXML private Label userNameLabel;
    @FXML private TextField currentWeightField;
    @FXML private TextField targetWeightField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label tageLabel;
    @FXML private Label calcResultLabel;

    // Christian: Felder der mittleren Spalte (Eintragen).
    @FXML private DatePicker datumPicker;
    @FXML private Label sollLabel;
    @FXML private TextField erreichteSchritteField;
    @FXML private Label saveInfoLabel;

    // Christian: Felder der rechten Spalte (Uebersicht).
    @FXML private Label uebersichtBenutzerLabel;
    @FXML private ComboBox<String> monatBox;
    @FXML private VBox uebersichtBox;
    @FXML private Label uebersichtSummeLabel;

    // Christian: laeuft automatisch nach dem Laden der FXML.
    @FXML
    public void initialize() {
        userNameLabel.setText("Angemeldet: " + Session.getUser());
        uebersichtBenutzerLabel.setText("Benutzer: " + Session.getUser());

        // Christian: Datumsfelder auf heute setzen.
        startDatePicker.setValue(LocalDate.now());
        datumPicker.setValue(LocalDate.now());

        sollLabel.setText("noch nicht berechnet");
        tageLabel.setText("0 Tage");

        // Christian: letzte 12 Monate in die Auswahl.
        YearMonth jetzt = YearMonth.now();
        for (int i = 0; i < 12; i++) {
            monatBox.getItems().add(jetzt.minusMonths(i).toString());
        }
        monatBox.setValue(jetzt.toString());

        zeigeTag();
        aktualisiereUebersicht();
    }

    // Christian: Datum in der Mitte gewechselt -> gespeicherte Schritte anzeigen.
    @FXML
    public void handleTagWechsel() {
        zeigeTag();
    }

    // Christian: einen Tag weiter springen.
    @FXML
    public void handleNextDay() {
        LocalDate aktuell = datumPicker.getValue();
        if (aktuell == null) {
            aktuell = LocalDate.now();
        }
        datumPicker.setValue(aktuell.plusDays(1));
        zeigeTag();
    }

    // Christian: gespeicherte Schritte fuer das Datum ins Feld holen (sonst leer).
    private void zeigeTag() {
        LocalDate datum = datumPicker.getValue();
        if (datum == null) {
            return;
        }
        int gespeichert = StepCsv.schritteFuer(Session.getUser(), datum);
        if (gespeichert > 0) {
            erreichteSchritteField.setText(String.valueOf(gespeichert));
        } else {
            erreichteSchritteField.clear();
        }
    }

    // Christian: Start- oder Enddatum geaendert -> Anzahl Tage anzeigen.
    @FXML
    public void handleDatumWechsel() {
        LocalDate start = startDatePicker.getValue();
        LocalDate ende = endDatePicker.getValue();
        if (start != null && ende != null) {
            long tage = ChronoUnit.DAYS.between(start, ende);
            tageLabel.setText(tage + " Tage");
        }
    }

    // Christian: LINKS - noetiges Schrittziel berechnen.
    @FXML
    public void handleCalcSteps() {
        try {
            double aktuell = Double.parseDouble(currentWeightField.getText());
            double wunsch = Double.parseDouble(targetWeightField.getText());

            LocalDate start = startDatePicker.getValue();
            LocalDate ende = endDatePicker.getValue();
            if (start == null || ende == null) {
                calcResultLabel.setText("Bitte Start- und Enddatum waehlen.");
                return;
            }

            long tage = ChronoUnit.DAYS.between(start, ende);
            tageLabel.setText(tage + " Tage");

            double abzunehmen = aktuell - wunsch; // kg

            if (abzunehmen <= 0 || tage <= 0) {
                calcResultLabel.setText(
                        "Bitte Wunschgewicht kleiner als aktuelles Gewicht "
                        + "und ein Enddatum nach dem Startdatum waehlen.");
                return;
            }

            // Christian: 1 kg Fett = ca. 7000 kcal.
            double gesamtDefizit = abzunehmen * 7000.0;

            // Christian: noetiges Defizit pro Tag.
            double defizitProTag = gesamtDefizit / tage;

            // Christian: kcal pro Schritt, grob aus dem Gewicht.
            // 10.000 Schritte ~ Gewicht * 5,8 kcal.
            double kcalProSchritt = aktuell * 5.8 / 10000.0;

            sollSchritte = Math.round(defizitProTag / kcalProSchritt);

            calcResultLabel.setText("ca. " + sollSchritte + " Schritte pro Tag");
            sollLabel.setText(sollSchritte + " Schritte");
            aktualisiereUebersicht();

        } catch (NumberFormatException e) {
            calcResultLabel.setText("Bitte fuer Gewicht Zahlen eingeben.");
        }
    }

    // Christian: MITTE - erreichte Schritte fuer einen Tag speichern.
    @FXML
    public void handleSaveSteps() {
        try {
            int erreicht = Integer.parseInt(erreichteSchritteField.getText());

            if (erreicht < 0) {
                saveInfoLabel.setText("Schritte duerfen nicht negativ sein.");
                return;
            }

            LocalDate datum = datumPicker.getValue();
            if (datum == null) {
                saveInfoLabel.setText("Bitte ein Datum waehlen.");
                return;
            }

            // Christian: in steps.csv speichern, Eintrag vom selben Tag wird ersetzt.
            StepCsv.saveOrReplace(new StepEntry(
                    Session.getUser(),
                    datum,
                    erreicht,
                    (int) sollSchritte));

            saveInfoLabel.setText("Gespeichert: " + erreicht
                    + " Schritte am " + datum + ".");

            aktualisiereUebersicht();

        } catch (NumberFormatException e) {
            saveInfoLabel.setText("Bitte eine Zahl eingeben.");
        }
    }

    // Christian: Eintrag fuer das gewaehlte Datum loeschen.
    @FXML
    public void handleResetTag() {
        LocalDate datum = datumPicker.getValue();
        if (datum == null) {
            saveInfoLabel.setText("Bitte ein Datum waehlen.");
            return;
        }
        StepCsv.delete(Session.getUser(), datum);
        erreichteSchritteField.clear();
        saveInfoLabel.setText("Eintrag fuer " + datum + " zurueckgesetzt.");
        aktualisiereUebersicht();
    }

    // Christian: anderer Monat gewaehlt -> Uebersicht neu aufbauen.
    @FXML
    public void handleMonatWechsel() {
        aktualisiereUebersicht();
    }

    // Christian: RECHTS - Monatsuebersicht Soll gegen Ist aufbauen.
    private void aktualisiereUebersicht() {
        String user = Session.getUser();
        List<StepEntry> alle = StepCsv.readAll();

        uebersichtBox.getChildren().clear();

        String monatText = monatBox.getValue();
        if (monatText == null) {
            return;
        }
        YearMonth monat = YearMonth.parse(monatText);

        long summeSoll = 0;
        long summeIst = 0;

        // Christian: jeden Tag des Monats durchgehen.
        for (int tagNr = 1; tagNr <= monat.lengthOfMonth(); tagNr++) {
            LocalDate tag = monat.atDay(tagNr);

            int ist = 0;
            int gespeichertesSoll = 0;

            for (StepEntry e : alle) {
                if (e.username.equals(user) && e.datum.equals(tag)) {
                    ist += e.schritte;
                    gespeichertesSoll = e.ziel;
                }
            }

            // Christian: Soll aus der Berechnung, sonst das gespeicherte Soll.
            long soll = (sollSchritte > 0) ? sollSchritte : gespeichertesSoll;

            summeSoll += soll;
            summeIst += ist;

            String haken = (soll > 0 && ist >= soll) ? "   OK" : "";
            Label zeile = new Label(
                    tag + "    Soll: " + soll + "    Ist: " + ist + haken);
            uebersichtBox.getChildren().add(zeile);
        }

        uebersichtSummeLabel.setText("Monat gesamt  ->  Soll: " + summeSoll
                + "    Ist: " + summeIst);
    }

    // Christian: zurueck ins Hauptmenue.
    @FXML
    public void handleBackToMenu(ActionEvent event) {
        changeView("mainMenu.fxml");
    }
}
