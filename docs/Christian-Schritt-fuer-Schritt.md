# Step Counter + Statistik - Schritt fuer Schritt (Christian)

Ganz einfache Erklaerung, in der Reihenfolge, wie man es baut und erklaeren kann.
Alle Kommentare im Code, die zu diesen Aenderungen gehoeren, fangen mit
`Christian:` an.

---

## Idee in einem Satz

Der Benutzer traegt pro Tag seine Schritte ein. Die Daten landen in einer
Text-Datei `steps.csv`. Eine zweite Seite zeigt die Schritte als Balkendiagramm.
Die Datenbank wird **nicht** angefasst (das ist Hasans Bereich).

---

## Reihenfolge der Bausteine

### 1. Wer ist eingeloggt? -> `model/Session.java` (neu)

- Kleine Klasse mit einem `static` Feld `currentUser`.
- `static` heisst: gibt es nur **einmal** im ganzen Programm.
- `setUser(name)` beim Login, `getUser()` ueberall sonst.
- **Warum:** Step Counter und Statistik muessen wissen, wessen Schritte gemeint sind.

### 2. Name beim Login merken -> `controller/LoginController.java` (1 Zeile)

- Nach erfolgreichem Login: `Session.setUser(usernameField.getText());`
- Import `com.fitapp.model.Session` ergaenzt.
- **Warum:** ab jetzt kennt `Session` den Namen.

### 3. Ein Datensatz -> `model/StepEntry.java` (neu)

- Nur ein Datenbehaelter: `username`, `datum`, `schritte`, `ziel`.
- Entspricht **einer Zeile** in `steps.csv`.
- Keine Logik, nur Felder + Konstruktor.

### 4. Datei lesen und schreiben -> `model/StepCsv.java` (neu)

- Liest/schreibt `src/main/resources/data/steps.csv`.
- Eine Zeile sieht so aus: `Rene,2026-09-01,3500,10000`
- Wichtige Methoden:
  - `readAll()` - Datei Zeile fuer Zeile lesen (`BufferedReader`), an den Kommas
    trennen (`split(",")`), in `StepEntry`-Objekte packen.
  - `append(e)` - eine Zeile hinten anhaengen (`BufferedWriter`).
  - `saveOrReplace(e)` - Tag speichern; ein alter Eintrag vom selben Tag wird
    vorher entfernt.
  - `delete(user, datum)` - Eintrag fuer einen Tag loeschen.
  - `schritteFuer(user, datum)` - Schritte an einem Tag (0 = nichts da).
- Beim ersten Benutzen legt ein `static`-Block Beispieldaten an
  (3 Benutzer, je 14 Tage), damit sofort etwas zu sehen ist.

### 5. Zahlen ausrechnen -> `model/Statistic.java` (ergaenzt)

- Der alte Teil (`period`, `numOfExercises`, leere Methoden) **bleibt**, weil
  `StatisticTest` ihn braucht.
- Neu: zweiter Konstruktor `Statistic(username, von, bis)`. Er liest `steps.csv`
  **einmal** ein und merkt sich die Liste.
- Neue Methoden gehen die Liste mit einer einfachen `for`-Schleife durch:
  - `schritteAmTag(tag)` - fuer das Balkendiagramm.
  - `schritteImZeitraum()`, `schritteProTag()`, `tageZielErreicht()`, ...
- **Warum getrennt vom Controller:** Rechnen gehoert ins Model, Anzeigen in den
  Controller (MVC).

### 6. Die Step-Counter-Seite -> `fxml/stepCounterTest.fxml` + `controller/StepCounterController.java` (beide neu geschrieben)

Drei Karten nebeneinander (`HBox` mit drei `VBox`):

- **LINKS - Rechner:** aktuelles Gewicht, Wunschgewicht, Start/Ende.
  Knopf `Schrittziel berechnen` -> `handleCalcSteps()`.
  Rechnung: 1 kg Fett ~ 7000 kcal, geteilt durch Tage = Defizit pro Tag,
  geteilt durch "kcal pro Schritt" = noetige Schritte pro Tag (das **Soll**).
- **MITTE - Eintragen:** Datum + erreichte Schritte (**Ist**).
  `Speichern` -> `handleSaveSteps()` ruft `StepCsv.saveOrReplace(...)`.
  `Tag zuruecksetzen` -> `handleResetTag()` ruft `StepCsv.delete(...)`.
  `Naechster Tag` springt einen Tag weiter.
- **RECHTS - Uebersicht:** Monat waehlbar (`ComboBox`). Fuer jeden Tag des Monats
  eine Zeile `Soll / Ist`, unten die Monatssumme. Baut `aktualisiereUebersicht()`.
- `initialize()` laeuft automatisch nach dem Laden der FXML: Felder vorbelegen,
  Monatsliste fuellen, erste Anzeige aufbauen.
- Verbindung FXML <-> Controller: `fx:id` im FXML = `@FXML`-Feld im Controller,
  `onAction="#methode"` = `@FXML`-Methode.

### 7. Die Statistik-Seite -> `fxml/statistics.fxml` + `controller/StatisticsController.java` (beide neu)

- Layout: Titel, Benutzer-Label, Monats-`ComboBox`, ein `BarChart`.
- `initialize()` fuellt die Monatsliste und ruft `zeichneDiagramm()`.
- `zeichneDiagramm()`:
  1. Monat aus der ComboBox lesen (`YearMonth.parse`).
  2. `new Statistic(name, ersterTag, letzterTag)`.
  3. Fuer jeden Tag `s.schritteAmTag(tag)` -> als `XYChart.Data` in eine
     `XYChart.Series`.
  4. `chart.getData().clear()` dann `chart.getData().add(serie)`.

### 8. Hinkommen: Navigation (kleine Aenderungen)

- `controller/MainMenuController.java`: `handleViewStatistics()` ruft jetzt
  `changeView("statistics.fxml")`.
- `navigation/Navigator.java`: eine `case`-Zeile mehr, setzt den Fenstertitel
  `StepCounter Statistik`.
- `fxml/mainMenu.fxml`: der Knopf heisst jetzt `StepCounter Statistik` und hat
  `onAction="#handleViewStatistics"` (vorher war das auskommentiert).

### 9. `data/steps.csv`

- Muss man **nicht** von Hand anlegen. `StepCsv` erzeugt die Datei beim ersten
  Start mit Beispieldaten.

---

## Starten und Testen

Starten (PowerShell, im Ordner `FitAppSchmidt`):

```
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
$mvn = "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.6.2\plugins\maven\lib\maven3\bin\mvn.cmd"
& $mvn "-Dcheckstyle.skip=true" "-Dmaven.buildNumber.doCheck=false" clean javafx:run
```

Anmelden: **`test01` / `1234`** (die Konten `Rene`/`Hasan`/`John` gehen nicht,
weil ihre Passwoerter in der DB gehasht sind - Klartext-Login passt nicht).

Testen:
1. Menue -> **Step Counter**. Links Gewicht + Datum, `Schrittziel berechnen`.
   Mitte: Ist eintragen, `Speichern`. Rechts: Monat waehlen, Soll/Ist pruefen.
2. Menue -> **StepCounter Statistik**. Monat waehlen, Balken pro Tag ansehen.

---

## Pruefungs-Stichworte (kurz erklaeren koennen)

- **MVC**: Model rechnet (`Statistic`, `StepCsv`), Controller steuert die Anzeige,
  FXML ist das Layout.
- **FXML <-> Controller**: `fx:controller`, `fx:id` + `@FXML`, `onAction="#..."`,
  `initialize()`.
- **Navigation**: `Navigator.changeView("x.fxml")` laedt FXML, setzt den
  Controller, tauscht die Scene, setzt den Titel per `switch`.
- **Datei lesen/schreiben**: `BufferedReader.readLine()` + `split(",")`;
  `BufferedWriter.write(...)` + `newLine()`; `try-with-resources`.
- **Datum**: `LocalDate`, `YearMonth` (`now`, `minusMonths`, `parse`, `atDay`,
  `lengthOfMonth`), `ChronoUnit.DAYS.between(...)`.
- **Diagramm**: `BarChart` mit `XYChart.Series` und `XYChart.Data`.
- **`static` (Session)**: existiert einmal pro Programmlauf - einfacher Speicher
  fuer "wer ist eingeloggt".
- **Warum CSV statt DB**: klein, flache Daten, keine Abhaengigkeit, Datenbank ist
  nicht mein Aufgabenbereich.
