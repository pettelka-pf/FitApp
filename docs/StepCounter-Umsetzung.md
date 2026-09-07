# StepCounter – Umsetzung

Kurzbeschreibung aller Dateien, die fuer den **Step Counter** und die
**StepCounter-Statistik** angelegt oder geaendert wurden. Fremde Bereiche
(Uebungen, Plaene, Kalorien, Datenbank, `StepTracker.java`) wurden **nicht**
angefasst.

## Neue Dateien

| Datei | Erklaerung |
|---|---|
| `src/main/java/com.fitapp/model/Session.java` | Merkt sich per `static`-Feld den Namen des angemeldeten Benutzers. Jede Seite kann so `Session.getUser()` fragen, ohne den Namen als Parameter weiterzureichen. |
| `src/main/java/com.fitapp/model/StepEntry.java` | Reiner Datenbehaelter fuer eine Zeile aus `steps.csv`: Benutzer, Datum, erreichte Schritte, Tagesziel. |
| `src/main/java/com.fitapp/model/StepCsv.java` | Liest und schreibt `steps.csv` mit `BufferedReader`/`BufferedWriter`. Bietet `append`, `saveOrReplace` (ueberschreibt den Tag), `delete`, `schritteFuer`, `readAll`, `alleBenutzer`. Beim ersten Zugriff werden ueber einen `static`-Block Beispieldaten fuer 14 Tage und drei Benutzer erzeugt. |
| `src/main/java/com.fitapp/controller/StatisticsController.java` | Controller der neuen Statistik-Seite. In `initialize()` wird die Monatsauswahl (letzte 12 Monate) gefuellt; `zeichneDiagramm()` baut aus `Statistic.schritteAmTag(...)` eine `XYChart.Series` und setzt sie in den `BarChart`. |
| `src/main/resources/fxml/statistics.fxml` | Layout der Statistik-Seite: Hintergrundbild, weisse Karte, Benutzer-Label, `ComboBox` fuer den Monat und ein `BarChart` mit `CategoryAxis` (Tag) und `NumberAxis` (Schritte). `fx:controller` zeigt auf `StatisticsController`. |
| `src/main/resources/data/steps.csv` | Wird von `StepCsv` beim ersten Start automatisch angelegt – nicht im Repo enthalten. |

## Ersetzte Dateien

| Datei | Erklaerung |
|---|---|
| `src/main/java/com.fitapp/model/Statistic.java` | Der bestehende Stub-Teil (`period`, `numOfExercises`, `calcStatistic`, `getAverage`, `export`) bleibt erhalten, damit `StatisticTest` weiter kompiliert. Neu: zweiter Konstruktor `Statistic(username, von, bis)`, der `steps.csv` einmal einliest, sowie Auswertungsmethoden (`schritteAmTag`, `schritteImZeitraum`, `geplanteSchritte`, `schritteDifferenz`, `verbrannteKalorien`, `schritteProTag`, `tageZielErreicht`). Alle rechnen mit einfachen `for`-Schleifen. |
| `src/main/java/com.fitapp/controller/StepCounterController.java` | Komplett neu: drei Spalten. **Links** Schrittzahlrechner (aus Gewichten und Zeitraum das noetige Tagesziel per Kaloriendefizit). **Mitte** erreichte Schritte fuer einen Tag speichern/zuruecksetzen (`StepCsv.saveOrReplace` / `delete`). **Rechts** Monatsuebersicht Soll gegen Ist mit Monatssumme. Nutzt `StepTracker` nicht mehr. |
| `src/main/resources/fxml/stepCounterTest.fxml` | Neues Layout mit `HBox` aus drei `VBox`-Karten passend zu den `@FXML`-Feldern und `onAction`-Methoden des neuen Controllers. |

## Kleine Aenderungen an vorhandenen Dateien

| Datei | Aenderung |
|---|---|
| `src/main/java/com.fitapp/controller/LoginController.java` | Import `com.fitapp.model.Session` ergaenzt; in `handleLogin(...)` nach `errorLabel.setVisible(false);` wird `Session.setUser(usernameField.getText())` gesetzt, damit Step Counter und Statistik den Namen kennen. |
| `src/main/java/com.fitapp/controller/MainMenuController.java` | `handleViewStatistics()` ruft jetzt `changeView("statistics.fxml")` auf. |
| `src/main/java/com.fitapp/navigation/Navigator.java` | Im `switch (fxmlFile)` neue Zeile `case "statistics.fxml" -> stage.setTitle("StepCounter Statistik");`. |
| `src/main/resources/fxml/mainMenu.fxml` | Button „View Statistics“ heisst jetzt „StepCounter Statistik“ und hat `onAction="#handleViewStatistics"` (vorher auskommentiert). |

## Wichtige Konzepte fuer die muendliche Pruefung

- **FXML ↔ Controller:** `fx:controller` verbindet Layout und Klasse. Elemente mit
  `fx:id` werden ueber `@FXML`-Felder gleichen Namens injiziert; Buttons rufen ueber
  `onAction="#methode"` die gleichnamige `@FXML`-Methode.
- **`initialize()`** wird vom `FXMLLoader` automatisch aufgerufen, nachdem alle
  `@FXML`-Felder gesetzt sind – hier werden ComboBoxen gefuellt und das Diagramm
  bzw. die Uebersicht zum ersten Mal aufgebaut.
- **`Navigator.changeView("x.fxml")`** laedt das FXML, setzt sich selbst per
  `setNavigator` in den Controller (der `implements Controller`), tauscht die Scene
  und setzt den Fenstertitel per `switch`.
- **CSV lesen/schreiben** mit `BufferedReader.readLine()` + `String.split(",")`
  bzw. `BufferedWriter.write(...)` / `newLine()`; `try-with-resources` schliesst
  den Stream automatisch.
- **`LocalDate` / `YearMonth`:** `YearMonth.now()`, `minusMonths(i)`, `parse(...)`,
  `atDay(n)`, `lengthOfMonth()`; Tagesdifferenz mit
  `ChronoUnit.DAYS.between(von, bis)`.
- **JavaFX `BarChart`:** eine `XYChart.Series<String, Number>` mit
  `XYChart.Data<>(kategorie, wert)` fuellen und mit `chart.getData().add(serie)`
  anzeigen; vorher `chart.getData().clear()`.
- **`static`-Feld (`Session`):** existiert einmal pro Programmlauf und eignet sich
  daher als einfacher „wer ist eingeloggt?“-Speicher.

## Build / Start

```
mvn -Dcheckstyle.skip=true -Dmaven.buildNumber.doCheck=false clean compile
mvn -Dcheckstyle.skip=true -Dmaven.buildNumber.doCheck=false clean javafx:run
```

`mvn test` ist auf `testing` schon vorher kaputt (`PlanTest.java`, fremder Bereich)
und wird hier nicht benutzt.
