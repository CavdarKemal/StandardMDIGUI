# StandardMDIGUI – Tutorial

> **Zielgruppe:** Erfahrene Java-Entwickler ohne Vorkenntnisse in Swing MDI  
> **Projekt:** [StandardMDIGUI auf GitHub](https://github.com/CavdarKemal/StandardMDIGUI)

---

## Inhaltsverzeichnis

1. [Was ist dieses Projekt?](#1-was-ist-dieses-projekt)
2. [Technologie-Stack](#2-technologie-stack)
3. [Grundkonzepte: Swing MDI](#3-grundkonzepte-swing-mdi)
4. [Projektstruktur](#4-projektstruktur)
5. [Voraussetzungen und Setup](#5-voraussetzungen-und-setup)
6. [Anwendung starten](#6-anwendung-starten)
7. [Das Kern-Muster: Design-View-Trennung](#7-das-kern-muster-design-view-trennung)
8. [Views verstehen und nutzen](#8-views-verstehen-und-nutzen)
9. [Konfigurationsverwaltung: AppConfig](#9-konfigurationsverwaltung-appconfig)
10. [Datenbankintegration: DatabaseView](#10-datenbankintegration-databaseview)
11. [Eigene View erstellen: Schritt für Schritt](#11-eigene-view-erstellen-schritt-für-schritt)
12. [Docker-Deployment](#12-docker-deployment)
13. [Tests](#13-tests)
14. [Beziehung zu TemplateGUI und ITSQ-Explorer](#14-beziehung-zu-templategui-und-itsq-explorer)
15. [Nächste Schritte](#15-nächste-schritte)

---

## 1. Was ist dieses Projekt?

**StandardMDIGUI** ist ein wiederverwendbares **Java Swing MDI-Framework** (Multiple Document Interface). Es stellt keine fertige Fachanwendung bereit, sondern eine sauber strukturierte Basis, auf der sich neue Desktop-Anwendungen effizient aufbauen lassen.

### Was das Framework bietet

| Funktion | Beschreibung |
|----------|-------------|
| **MDI-Desktop** | Mehrere interne Fenster in einem Hauptfenster |
| **View-Registrierung** | Neue Views deklarativ einbinden — Menü und Toolbar entstehen automatisch |
| **Design-View-Trennung** | GUI-Aufbau und Geschäftslogik strikt getrennt |
| **AppConfig** | Persistente, typsichere Konfigurationsverwaltung |
| **DatabaseView** | Generischer SQL-Client für beliebige JDBC-Datenquellen |
| **Background-Tasks** | SwingWorker-Integration mit Fortschrittsanzeige |
| **Docker** | PostgreSQL + GUI-App containerisiert |

### Projektlinie

```
StandardMDIGUI  →  TemplateGUI  →  ITSQ-Explorer
  (Framework)      (Template)       (Fachanwendung)
```

StandardMDIGUI ist der Ausgangspunkt: das sauberste und modernste Glied der Kette (Java 23).

---

## 2. Technologie-Stack

| Komponente | Details |
|------------|---------|
| **Java** | 23 |
| **GUI** | Java Swing — kein externes UI-Framework |
| **Build** | Maven 3.9+ |
| **Datenbank** | PostgreSQL 16 (via JDBC) |
| **JSON** | Jackson 2.17 |
| **Logging** | SLF4J 2.0 + Logback 1.4 |
| **Testing** | JUnit 5 + AssertJ Swing 3.17 |
| **Container** | Docker + Docker Compose |

---

## 3. Grundkonzepte: Swing MDI

### JDesktopPane und JInternalFrame

Ein MDI-Desktop besteht aus zwei Schlüsselklassen:

- **`JDesktopPane`**: Der Desktop-Bereich — ein Container, in dem interne Fenster schwimmen.
- **`JInternalFrame`**: Ein internes Fenster innerhalb des Desktops — hat eigene Titelleiste, kann minimiert, maximiert und verschoben werden.

```
JFrame (Hauptfenster)
└── JDesktopPane (Desktop)
    ├── JInternalFrame (View 1)
    ├── JInternalFrame (View 2)
    └── JInternalFrame (View 3)
```

### Event Dispatch Thread (EDT)

Swing ist **nicht thread-sicher**: Alle GUI-Operationen müssen im EDT ausgeführt werden. Lange laufende Operationen (Datenbankabfragen, Dateioperationen) müssen in Worker-Threads ausgelagert werden — sonst friert die GUI ein.

Das Framework kapselt das über `BaseView.executeTask()` mit SwingWorker.

---

## 4. Projektstruktur

```
StandardMDIGUI/
├── src/main/java/de/cavdar/
│   ├── frame/
│   │   └── MainFrame.java              # MDI-Hauptfenster mit View-Registrierung
│   ├── view/                            # Logik-Schicht (kein GUI-Aufbau)
│   │   ├── ViewInfo.java               # Interface: Metadaten einer View
│   │   ├── BaseView.java               # Abstrakte Basisklasse für alle Views
│   │   ├── SampleView.java             # Beispiel-View
│   │   ├── DatabaseView.java           # JDBC-SQL-Client
│   │   ├── CustomerTreeView.java       # Hierarchischer Baum (Kunden/Szenarien)
│   │   ├── TreeView.java               # Basis für Baum-Views
│   │   ├── AnalyseView.java            # Vorlage
│   │   └── ProzessView.java            # Vorlage
│   ├── design/                          # GUI-Schicht (nur Layout, keine Logik)
│   │   ├── EmbeddablePanel.java        # Abstrakte Basis für einbettbare Panels
│   │   ├── BaseViewPanel.java          # Standard-Panel für Views
│   │   ├── DesktopPanel.java           # JDesktopPane-Container
│   │   ├── SettingsPanel.java          # Einstellungs-Panel (links)
│   │   ├── TreePanel.java              # Navigations-Baum (links)
│   │   ├── DatabaseViewPanel.java      # SQL-Editor und Ergebnis-Tabelle
│   │   └── CustomerTreeViewPanel.java  # Kunden-Baum mit Checkboxen
│   ├── model/
│   │   ├── AppConfig.java              # Singleton: Konfigurationsverwaltung
│   │   ├── ConnectionInfo.java         # JDBC-Verbindungsdaten
│   │   ├── ConfigEntry.java            # Record für Config-Einträge
│   │   ├── TestCustomer.java           # Domänenmodell: Kunde
│   │   ├── TestScenario.java           # Domänenmodell: Testszenario
│   │   └── TestCrefo.java              # Domänenmodell: Testfall
│   ├── util/
│   │   ├── ConnectionManager.java      # Zentrale DB-Verbindungsverwaltung
│   │   ├── IconLoader.java             # Icons aus Resources laden
│   │   └── TestDataLoader.java         # JSON-basierter Datenlader
│   └── exception/
│       ├── ConfigurationException.java
│       └── ViewException.java
├── src/main/resources/
│   └── icons/                           # PNG-Icons für Views
├── config.properties                    # Hauptkonfiguration
├── Dockerfile
└── docker-compose.yml
```

---

## 5. Voraussetzungen und Setup

### Systemvoraussetzungen

- JDK 23 oder neuer
- Maven 3.9+
- Docker Desktop (optional, nur für PostgreSQL)

### Bauen

```cmd
cd E:\Projekte\ClaudeCode\StandardMDIGUI
ci.cmd 25
```

---

## 6. Anwendung starten

### Aus der IDE

Hauptklasse: `de.cavdar.frame.MainFrame` (oder die Klasse mit der `main()`-Methode)

VM-Optionen empfohlen:

```
-Dfile.encoding=UTF-8 -Xms512m -Xmx2g
```

### Als JAR

```cmd
java -jar target/standard-mdi-gui-app-1.0-SNAPSHOT.jar
```

### Mit eigener Konfiguration

```cmd
java -Dconfig.file=C:\MeinProjekt\meine-config.properties -jar app.jar
```

### Konfigurationsprioritäten

1. System-Property: `-Dconfig.file=/pfad/zur/config.properties`
2. Umgebungsvariable: `CONFIG_FILE_PATH=/pfad/zur/config.properties`
3. Standard: `config.properties` im Arbeitsverzeichnis

---

## 7. Das Kern-Muster: Design-View-Trennung

Das wichtigste Architekturmuster des Frameworks ist die strikte Trennung zwischen GUI-Aufbau und Geschäftslogik.

### Zwei Klassen für jede View

Jede View besteht aus genau zwei Klassen:

| Klasse | Basis | Paket | Verantwortlichkeit |
|--------|-------|-------|-------------------|
| `*Panel` | `BaseViewPanel` | `design/` | GUI-Komponenten, Layout — **keine Logik** |
| `*View` | `BaseView` | `view/` | Ereignisbehandlung, Geschäftslogik — **kein Layout** |

### Warum diese Trennung?

- **Panel** kann mit GUI-Buildern (IntelliJ Form Designer, NetBeans) bearbeitet werden.
- **View** kann Unit-getestet werden ohne GUI.
- Änderungen am Layout berühren nie die Logik — und umgekehrt.

### Wie sie zusammenarbeiten

```java
// View erstellt ihr Panel:
public class MeineView extends BaseView {

    private MeinPanel meinPanel;

    @Override
    protected BaseViewPanel createPanel() {
        meinPanel = new MeinPanel();  // Panel wird erstellt
        return meinPanel;             // Framework platziert es im Fenster
    }

    @Override
    protected void setupToolbarActions() {
        // View verbindet sich mit Panel-Komponenten:
        meinPanel.getActionButton().addActionListener(e -> doAction());
    }
}

// Panel kümmert sich nur ums Layout:
public class MeinPanel extends BaseViewPanel {
    private JButton actionButton;

    @Override
    protected void initializePanel() {
        actionButton = new JButton("Aktion");
        getContentPanel().add(actionButton);
    }

    public JButton getActionButton() { return actionButton; }
}
```

### BaseView im Detail

```java
public abstract class BaseView extends JInternalFrame implements ViewInfo {

    // Muss überschrieben werden: Panel erstellen
    protected abstract BaseViewPanel createPanel();

    // Muss überschrieben werden: Toolbar-Aktionen einrichten
    protected abstract void setupToolbarActions();

    // Optional: weitere Listener einrichten
    protected void setupListeners() {}

    // Bereitgestellt: Hintergrundaufgabe mit SwingWorker ausführen
    protected void executeTask(Runnable task) { ... }
}
```

### ViewInfo-Interface: Metadaten einer View

```java
public interface ViewInfo {
    String getMenuLabel();          // Anzeigename im Menü (Pflicht)
    String getToolbarLabel();       // Text im Toolbar-Button (null = kein Button)
    Icon getIcon();                 // Icon (null = kein Icon)
    KeyStroke getKeyboardShortcut(); // Tastenkürzel (null = keins)
    String getMenuGroup();          // Untermenü-Gruppe (null = kein Untermenü)
}
```

---

## 8. Views verstehen und nutzen

### Hauptfenster-Layout

```
┌──────────────────────────────────────────────────────────┐
│  Menüleiste: Datei │ Ansicht │ Hilfe                     │
│  Toolbar: [Config ▼] [DB ▼] [Testquelle ▼] | [View-Buttons]│
├────────────┬─────────────────────────────────────────────┤
│            │                                              │
│  LINKES    │            DESKTOP                           │
│  PANEL     │     ┌───────────────┐ ┌───────────────┐    │
│            │     │  View 1       │ │  View 2       │    │
│  Einstellg.│     │               │ │               │    │
│  Baum-Nav. │     └───────────────┘ └───────────────┘    │
│            │                                              │
└────────────┴─────────────────────────────────────────────┘
```

### Registrierte Views

| View | Kürzel | Beschreibung |
|------|--------|-------------|
| **SampleView** | Strg+1 | Beispiel: Button startet Hintergrundaufgabe |
| **DatabaseView** | — | Generischer SQL-Client |
| **CustomerTreeView** | Strg+5 | Hierarchischer Kunden-/Szenario-Baum |

### View öffnen

- Über das **Menü**: `Ansicht → [View-Name]`
- Über den **Toolbar-Button** (falls registriert)
- Per **Tastenkürzel**

### Fensteranordnung

Im Menü unter `Fenster`:
- **Nebeneinander** — Alle Views nebeneinander anordnen
- **Übereinander** — Alle Views untereinander
- **Kaskade** — Views gestaffelt überlagern

---

## 9. Konfigurationsverwaltung: AppConfig

### Grundprinzip

`AppConfig` ist ein **Singleton**, das eine `.properties`-Datei verwaltet. Es speichert alle Anwendungseinstellungen (Fensterposition, zuletzt verwendete Werte, Datenbankverbindungen) und schreibt Änderungen automatisch zurück.

### Verwendung

```java
AppConfig cfg = AppConfig.getInstance();

// Lesen
String pfad    = cfg.getProperty("TEST-BASE-PATH");
String[] quell = cfg.getArray("TEST-SOURCES");       // semikolontrennt
boolean admin  = cfg.getBool("ADMIN_FUNCS_ENABLED");
int timeout    = cfg.getInt("TIMEOUT", 5000);         // mit Standardwert

// Schreiben (automatisch persistiert)
cfg.setProperty("LAST_USED_DIR", "/mein/verzeichnis");
cfg.save();
```

### Eigenschaftsgruppen

Die gespeicherte Datei ist in Gruppen gegliedert — dadurch bleibt sie lesbar:

```properties
# ─── WINDOW ───────────────────────────────────────────────
LAST_WINDOW_HEIGHT=825
LAST_WINDOW_WIDTH=1428
LAST_MAIN_SPLIT_DIVIDER=300

# ─── LATEST ───────────────────────────────────────────────
LAST_DB_CONNECTION=MeineDB
LAST_CONFIG_FILE=config.properties

# ─── FLAGS ────────────────────────────────────────────────
ADMIN_FUNCS_ENABLED=true

# ─── DATABASE ─────────────────────────────────────────────
DB_CONNECTIONS=MeineDB|org.postgresql.Driver|jdbc:postgresql://localhost:5432/mydb|user|cGFzc3dvcmQ=
```

### Konfigurationsdatei zur Laufzeit wechseln

In der Toolbar gibt es eine ComboBox für die Konfigurationsdatei. Beim Wechsel:
1. Neue Datei wird geladen.
2. GUI-Elemente aktualisieren sich automatisch.
3. Folge-Speicherungen gehen in die neue Datei.

Das ermöglicht verschiedene Setups (Entwicklung, Test, Produktion) ohne Neustart.

---

## 10. Datenbankintegration: DatabaseView

### Verbindung einrichten

1. In der DatabaseView: Reiter **Verbindungen**
2. **Neu** klicken und ausfüllen:

| Feld | Beispiel |
|------|---------|
| Name | `Lokale PostgreSQL` |
| Treiber | `org.postgresql.Driver` |
| URL | `jdbc:postgresql://localhost:5432/meindb` |
| Benutzer | `postgres` |
| Passwort | `geheim` |

3. **Speichern** — die Verbindung wird in `config.properties` unter `DB_CONNECTIONS` gespeichert (Passwort Base64-kodiert).

### SQL ausführen

1. Verbindung in der ComboBox auswählen → **Verbinden**
2. SQL eingeben und **Ausführen** (oder Strg+Enter)
3. Ergebnisse erscheinen in der Tabelle

Unterstützte Datenbanken:

| Datenbank | JDBC-Treiber |
|-----------|-------------|
| PostgreSQL | `org.postgresql.Driver` |
| MySQL | `com.mysql.cj.jdbc.Driver` |
| Oracle | `oracle.jdbc.OracleDriver` |
| SQL Server | `com.microsoft.sqlserver.jdbc.SQLServerDriver` |
| H2 | `org.h2.Driver` |
| SQLite | `org.sqlite.JDBC` |

### Tabellen-Browser

Der linke Baum zeigt alle verfügbaren Tabellen. Ein Klick auf eine Tabelle lädt die Spaltennamen. SQL-Vorlagen wie `SELECT * FROM tabelle LIMIT 100` können automatisch generiert werden.

### Export

Abfrageergebnisse können als **CSV** exportiert werden.

---

## 11. Eigene View erstellen: Schritt für Schritt

### Schritt 1: Panel-Klasse erstellen

```java
// src/main/java/de/cavdar/design/MeinViewPanel.java
package de.cavdar.design;

import javax.swing.*;

public class MeinViewPanel extends BaseViewPanel {

    private JButton btnAktion;
    private JLabel  lblStatus;
    private JTextArea taErgebnis;

    @Override
    protected void initializePanel() {
        // Layout aufbauen
        JPanel controls = new JPanel();
        btnAktion = new JButton("Starten");
        lblStatus = new JLabel("Bereit");
        controls.add(btnAktion);
        controls.add(lblStatus);

        taErgebnis = new JTextArea(10, 40);
        taErgebnis.setEditable(false);

        getContentPanel().setLayout(new BorderLayout());
        getContentPanel().add(controls, BorderLayout.NORTH);
        getContentPanel().add(new JScrollPane(taErgebnis), BorderLayout.CENTER);
    }

    // Getter für die View-Klasse:
    public JButton getAktionButton() { return btnAktion; }
    public JLabel  getStatusLabel()  { return lblStatus; }
    public JTextArea getErgebnisArea() { return taErgebnis; }
}
```

### Schritt 2: View-Klasse erstellen

```java
// src/main/java/de/cavdar/view/MeinView.java
package de.cavdar.view;

import de.cavdar.design.BaseViewPanel;
import de.cavdar.design.MeinViewPanel;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class MeinView extends BaseView {

    private MeinViewPanel panel;

    public MeinView() {
        super("Meine View");
    }

    @Override
    protected BaseViewPanel createPanel() {
        panel = new MeinViewPanel();
        return panel;
    }

    @Override
    protected void setupToolbarActions() {
        panel.getAktionButton().addActionListener(e -> startAktion());
    }

    // ViewInfo-Methoden:

    @Override
    public String getMenuLabel() { return "Meine View"; }

    @Override
    public String getToolbarLabel() { return "Mein Tool"; }

    @Override
    public Icon getIcon() { return IconLoader.load("my-icon.png"); }

    @Override
    public KeyStroke getKeyboardShortcut() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK);
    }

    @Override
    public String getMenuGroup() { return "Meine Gruppe"; }

    // Geschäftslogik:

    private void startAktion() {
        panel.getStatusLabel().setText("Läuft...");

        executeTask(() -> {
            // Hier läuft der Code im Worker-Thread:
            String ergebnis = langsameOperation();

            // GUI-Updates nur über invokeLater:
            SwingUtilities.invokeLater(() -> {
                panel.getErgebnisArea().setText(ergebnis);
                panel.getStatusLabel().setText("Fertig");
            });
        });
    }

    private String langsameOperation() {
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        return "Ergebnis der Aktion";
    }
}
```

### Schritt 3: View im Hauptfenster registrieren

```java
// In MainFrame (oder der Klasse mit main()):
mainFrame.registerView(MeinView::new);
```

Das war's. Die View erscheint automatisch:
- Im Menü unter `Ansicht → Meine Gruppe → Meine View`
- Als Toolbar-Button mit Icon
- Mit Tastenkürzel Strg+M

### Hintergrundaufgaben mit `executeTask`

```java
executeTask(() -> {
    // Worker-Thread: Datenbankabfrage, Datei laden, ...
    List<String> daten = datenbankAbfragen();

    SwingUtilities.invokeLater(() -> {
        // EDT: GUI aktualisieren
        panel.getTabelle().setModel(new DefaultTableModel(daten));
    });
});
```

`executeTask` zeigt automatisch:
- Fortschrittsbalken (unbestimmt) während der Ausführung
- Abbrechen-Button
- Fehlerdialog bei Exceptions

---

## 12. Docker-Deployment

### Für GUI-Anwendungen: X11-Forwarding

Swing-GUIs in Docker benötigen einen X11-Server auf dem Host.

**Windows:** [VcXsrv](https://sourceforge.net/projects/vcxsrv/) installieren und mit **"Disable access control"** starten.

```cmd
cd docker
start-windows.bat
```

**Linux:** Direktes X11 ohne Zusatzsoftware:

```bash
xhost +local:docker
docker-compose up --build
```

### PostgreSQL-Container für Entwicklung

```cmd
docker-compose up postgres -d
```

Verbindungsdetails (aus dem Container heraus):

| Parameter | Wert |
|-----------|------|
| Host | `postgres` (Containername) |
| Port | `5432` |
| Datenbank | `standardmdi` |
| Benutzer | `postgres` |
| Passwort | `postgres` |

### Distribution bauen

```cmd
mvn clean package
```

Erstellt: `target/StandardMDIGUI-1.0-SNAPSHOT-distribution.zip`

```
StandardMDIGUI-1.0-SNAPSHOT/
├── StandardMDIGUI-1.0-SNAPSHOT.jar
├── lib/                              (alle Abhängigkeiten)
├── start.bat                         (Windows)
└── start.sh                          (Linux/macOS)
```

---

## 13. Tests

### GUI-Tests mit AssertJ Swing

```java
@Test
public void shouldOpenDatabaseView() {
    mainFrameFixture.menuItemWithPath("Ansicht", "Datenbank").click();
    mainFrameFixture.internalFrame("Datenbank").requireVisible();
}
```

### Tests ausführen

```cmd
cit.cmd 25
```

> **Hinweis:** GUI-Tests benötigen eine Display-Umgebung. In CI-Systemen wird `Xvfb` (virtueller Framebuffer) benötigt. Das `pom.xml` enthält bereits die nötigen JVM-Flags für Java 23.

### Unit-Tests für AppConfig

```java
@Test
public void shouldPersistProperty() {
    AppConfig cfg = AppConfig.getInstance();
    cfg.setProperty("TEST_KEY", "TEST_VALUE");
    cfg.save();

    // Neu laden
    AppConfig.resetInstance();
    assertEquals("TEST_VALUE", AppConfig.getInstance().getProperty("TEST_KEY"));
}
```

---

## 14. Beziehung zu TemplateGUI und ITSQ-Explorer

StandardMDIGUI ist der **Kern** einer Familie von Swing-Projekten:

```
StandardMDIGUI (Java 23)
│   Framework: MDI-Infrastruktur, Design-View-Muster, AppConfig, Docker
│
├─► TemplateGUI (Java 17)
│       Erweitert um: ProzessView, ItsqTreeView, ItsqExplorerView
│       Maven-Artefakt-Integration für Testdaten
│
└─► ITSQ-Explorer (Java 17)
        Spezialisiert: Migrationstools, Umgebungs-Locking
        Basiert auf TemplateGUI-Architektur
```

**Für neue Projekte** empfiehlt sich StandardMDIGUI als Basis — es ist das modernste und sauberste Glied der Kette.

---

## 15. Nächste Schritte

### CustomerTreeView: Hierarchische Daten anzeigen

Die mitgelieferte `CustomerTreeView` zeigt, wie ein drei-stufiger Baum (Kunde → Szenario → Testfall) mit Checkboxen aufgebaut wird. Sie dient als Vorlage für eigene Baumstrukturen.

### ConnectionManager: Verbindungen verwalten

```java
ConnectionManager cm = ConnectionManager.getInstance();
Connection conn = cm.getConnection("MeineDB");
// ... SQL ausführen ...
cm.releaseConnection("MeineDB");
```

### Weiterführende Dokumentation

| Dokument | Inhalt |
|----------|--------|
| `docs/CLAUDE_CONTEXT.md` | Entwicklungsgeschichte und Feature-Notizen |
| `docs/DOCKER_GUIDE.md` | Docker-Setup im Detail |

---

*Erstellt: April 2026*
