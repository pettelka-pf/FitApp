package com.fitapp.controller;

import com.fitapp.controller.Controller;
import com.fitapp.model.Session;
import com.fitapp.model.Statistic;
import com.fitapp.navigation.Navigator;
import com.fitapp.util.BackgroundImageHelper;

import javafx.fxml.FXML;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Christian: Seite "StepCounter Statistik".
 *
 * Man waehlt einen Monat. Das Balkendiagramm zeigt pro Tag
 * entweder die erreichten Schritte oder die aufgenommenen
 * Kalorien des angemeldeten Benutzers.
 *
 * Zusaetzlich kann die aktuell angezeigte Statistik
 * als JSON exportiert oder ueber den Windows-Druckdialog
 * ausgedruckt werden.
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

    // Statistik-Karte, die gedruckt werden soll.
    @FXML private VBox statistikKarte;

    // Buttons, die nicht gedruckt werden sollen.
    @FXML private Button jsonExportButton;
    @FXML private Button druckenButton;
    @FXML private Button backButton;

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

        monatBox.setValue(
                jetzt.toString()
        );

        // Standardmaessig Schritte anzeigen.
        yAxis.setLabel(
                "Erreichte Schritte"
        );

        statistikWechselButton.setText(
                "Kalorien anzeigen"
        );

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

        YearMonth monat =
                YearMonth.parse(monatText);

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

            serie.setName(
                    "Aufgenommene Kalorien"
            );

        } else {

            serie.setName(
                    "Erreichte Schritte"
            );
        }

        for (int tagNr = 1;
             tagNr <= monat.lengthOfMonth();
             tagNr++) {

            LocalDate tag =
                    monat.atDay(tagNr);

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

        chart.getData().add(
                serie
        );
    }


    // =========================================================
    // JSON EXPORT
    // =========================================================

    /**
     * Aktuell angezeigte Statistik als JSON exportieren.
     */
    @FXML
    public void handleJsonExport() {

        if (!Session.isLoggedIn()) {
            return;
        }

        String monatText =
                monatBox.getValue();

        if (monatText == null) {
            return;
        }

        YearMonth monat =
                YearMonth.parse(monatText);

        String statistikName;

        if (zeigeKalorien) {
            statistikName = "Kalorien";
        } else {
            statistikName = "Schritte";
        }

        String dateiname =
                "Statistik_"
                        + monat
                        + "_"
                        + statistikName
                        + ".json";

        FileChooser fileChooser =
                new FileChooser();

        fileChooser.setTitle(
                "Statistik als JSON speichern"
        );

        fileChooser.setInitialFileName(
                dateiname
        );

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "JSON-Dateien (*.json)",
                        "*.json"
                )
        );

        File datei =
                fileChooser.showSaveDialog(
                        chart.getScene().getWindow()
                );

        if (datei == null) {
            return;
        }

        if (!datei.getName()
                .toLowerCase()
                .endsWith(".json")) {

            datei = new File(
                    datei.getAbsolutePath()
                            + ".json"
            );
        }

        try {

            exportiereAlsJson(
                    datei,
                    monat
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    /**
     * Erstellt die JSON-Datei.
     */
    private void exportiereAlsJson(
            File datei,
            YearMonth monat
    ) throws IOException {

        Statistic s = new Statistic(
                Session.getUserId(),
                monat.atDay(1),
                monat.atDay(monat.lengthOfMonth())
        );

        StringBuilder json =
                new StringBuilder();

        json.append("{\n");

        json.append("  \"benutzer\": \"")
                .append(
                        escapeJson(
                                Session.getUsername()
                        )
                )
                .append("\",\n");

        json.append("  \"monat\": \"")
                .append(monat)
                .append("\",\n");

        json.append("  \"statistik\": \"")
                .append(
                        zeigeKalorien
                                ? "Kalorien"
                                : "Schritte"
                )
                .append("\",\n");

        json.append("  \"eintraege\": [\n");

        for (int tagNr = 1;
             tagNr <= monat.lengthOfMonth();
             tagNr++) {

            LocalDate tag =
                    monat.atDay(tagNr);

            Number wert;

            if (zeigeKalorien) {

                wert = s.kalorienAmTag(tag);

            } else {

                wert = s.schritteAmTag(tag);
            }

            json.append("    {\n");

            json.append("      \"tag\": ")
                    .append(tagNr)
                    .append(",\n");

            json.append("      \"datum\": \"")
                    .append(tag)
                    .append("\",\n");

            json.append("      \"wert\": ")
                    .append(wert)
                    .append("\n");

            json.append("    }");

            if (tagNr < monat.lengthOfMonth()) {
                json.append(",");
            }

            json.append("\n");
        }

        json.append("  ]\n");
        json.append("}\n");

        try (FileWriter writer =
                     new FileWriter(
                             datei,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    json.toString()
            );
        }
    }


    /**
     * Sonderzeichen fuer JSON escapen.
     */
    private String escapeJson(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }


    // =========================================================
    // DRUCKEN / WINDOWS PRINT TO PDF
    // =========================================================

    /**
     * Oeffnet den Windows-Druckdialog.
     *
     * Der Benutzer kann dort z.B.
     * "Microsoft Print to PDF" auswaehlen.
     */
    @FXML
    public void handleDrucken() {

        if (!Session.isLoggedIn()) {
            return;
        }

        if (monatBox.getValue() == null) {
            return;
        }

        Printer printer =
                Printer.getDefaultPrinter();

        if (printer == null) {

            System.out.println(
                    "Kein Drucker gefunden."
            );

            return;
        }

        PrinterJob printerJob =
                PrinterJob.createPrinterJob(
                        printer
                );

        if (printerJob == null) {

            System.out.println(
                    "Druckauftrag konnte nicht erstellt werden."
            );

            return;
        }

        /*
         * A4 Querformat.
         */
        PageLayout pageLayout =
                printer.createPageLayout(
                        Paper.A4,
                        PageOrientation.LANDSCAPE,
                        Printer.MarginType.DEFAULT
                );

        printerJob.getJobSettings()
                .setPageLayout(pageLayout);

        /*
         * Name des Druckauftrags.
         */
        printerJob.getJobSettings()
                .setJobName(
                        "Statistik_"
                                + monatBox.getValue()
                );

        /*
         * Druckdialog anzeigen.
         */
        boolean dialogOk =
                printerJob.showPrintDialog(
                        chart.getScene().getWindow()
                );

        /*
         * Benutzer hat abgebrochen.
         */
        if (!dialogOk) {

            printerJob.cancelJob();

            return;
        }

        /*
         * Merken, welche Elemente sichtbar waren.
         */
        boolean jsonVisible =
                jsonExportButton != null
                        && jsonExportButton.isVisible();

        boolean druckenVisible =
                druckenButton != null
                        && druckenButton.isVisible();

        boolean backVisible =
                backButton != null
                        && backButton.isVisible();

        try {

            /*
             * Bedienelemente ausblenden.
             *
             * Der Monat bleibt NICHT einfach unsichtbar,
             * sondern wird weiter unten als Text angezeigt.
             */
            monatBox.setVisible(false);

            if (jsonExportButton != null) {
                jsonExportButton.setVisible(false);
            }

            if (druckenButton != null) {
                druckenButton.setVisible(false);
            }

            if (backButton != null) {
                backButton.setVisible(false);
            }

            /*
             * Einen Drucktitel mit Monat und Statistikart
             * temporär unter dem Benutzernamen anzeigen.
             *
             * Dafür erzeugen wir einen zusätzlichen Label-
             * Knoten direkt in der Statistik-Karte.
             */
            Label druckMonatLabel =
                    new Label();

            druckMonatLabel.setText(
                    "Monat: "
                            + monatBox.getValue()
                            + "    |    Statistik: "
                            + (
                            zeigeKalorien
                                    ? "Aufgenommene Kalorien"
                                    : "Erreichte Schritte"
                    )
            );

            druckMonatLabel.setStyle(
                    "-fx-font-weight: bold;"
            );

            /*
             * Das Label nach dem Benutzer-Label einfuegen.
             */
            int userLabelIndex =
                    statistikKarte
                            .getChildren()
                            .indexOf(userNameLabel);

            statistikKarte
                    .getChildren()
                    .add(
                            userLabelIndex + 1,
                            druckMonatLabel
                    );

            /*
             * CSS und Layout aktualisieren.
             */
            statistikKarte.applyCss();
            statistikKarte.layout();

            /*
             * Statistik-Karte drucken.
             */
            boolean gedruckt =
                    printerJob.printPage(
                            pageLayout,
                            statistikKarte
                    );

            /*
             * Druckauftrag abschliessen.
             */
            if (gedruckt) {

                printerJob.endJob();

            } else {

                printerJob.cancelJob();

                System.out.println(
                        "Druckvorgang fehlgeschlagen."
                );
            }

            /*
             * Temporäres Drucklabel wieder entfernen.
             */
            statistikKarte
                    .getChildren()
                    .remove(
                            druckMonatLabel
                    );

        } finally {

            /*
             * Urspruenglichen Zustand wiederherstellen.
             */
            monatBox.setVisible(true);

            if (jsonExportButton != null) {
                jsonExportButton.setVisible(
                        jsonVisible
                );
            }

            if (druckenButton != null) {
                druckenButton.setVisible(
                        druckenVisible
                );
            }

            if (backButton != null) {
                backButton.setVisible(
                        backVisible
                );
            }

            /*
             * Layout wiederherstellen.
             */
            statistikKarte.applyCss();
            statistikKarte.layout();
        }
    }


    // =========================================================
    // ZURUECK
    // =========================================================

    // Zurueck ins Hauptmenue.
    @FXML
    public void handleBackToMenu() {
        changeView("mainMenu.fxml");
    }
}
